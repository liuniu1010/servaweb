package org.neo.servaweb.webservice;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.nio.charset.StandardCharsets;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.impl.CoderBotForUIImpl;
import org.neo.servaaiagent.impl.AccountAgentImpl;

@Path("/aicoderbot")
public class AICoderBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AICoderBot.class);

    private String downloadFolder = "download";

    protected String getOnlineFileAbsolutePath() {
        return super.getAbsoluteResourcePath() + File.separator + downloadFolder;
    }

    protected String getRelevantVisitPath() {
        return downloadFolder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return CoderBotForUIImpl.getInstance(getOnlineFileAbsolutePath(), getRelevantVisitPath());
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse send(WSModel.AIChatParams params) {
        return super.send(params);
    }

    @POST
    @Path("/streamsend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamsend(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        ServletOutputStream outputStream = null;
        StreamCallbackImpl notifyCallback = null;
        try {
            checkSessionValid(params.getSession());

            outputStream = response.getOutputStream();
            notifyHistory(params.getSession(), outputStream);
            notifyCallback = new AICoderBot.StreamCallbackImpl(params, outputStream);
            StreamCache.getInstance().put(params.getSession(), notifyCallback);
            WSModel.AIChatResponse chatResponse = super.streamsend(params, notifyCallback);

            String information = "";
            if(chatResponse.getIsSuccess()) {
                information = "Code generated success, " + chatResponse.getMessage();
            }
            else {
                information = "Failed to generate code due to: " + chatResponse.getMessage();
            }
            notifyCallback.notify(information);
            // virtualStreamsend(notifyCallback);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_SESSION_INVALID) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid session");
                response.flushBuffer();
                return;
            }
        }
        catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        finally {
            StreamCache.getInstance().remove(params.getSession()); // this will close the associated outputstream
        }
    }

    private void checkSessionValid(String session) {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.checkLogin(session); 
    }

    private void virtualStreamsend(NotifyCallbackIFC notifyCallback) {
        for(int i = 0;i < 50;i++) {
            String information = "Begin to process step " + i + "...";
            notifyCallback.notify(information);
            try {
                Thread.sleep(1000);
            }
            catch(Exception ex) {
            }
        }
        notifyCallback.notify("Process completed!");
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(WSModel.AIChatParams params) {
        return super.echo(params);
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(WSModel.AIChatParams params) {
        return super.newchat(params);
    }

    @POST
    @Path("/streamrefresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamrefresh(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try {
            OutputStream outputStream = response.getOutputStream();
            notifyHistory(params.getSession(), outputStream);
            StreamCallbackImpl streamCallback = StreamCache.getInstance().get(params.getSession());
            if(streamCallback == null) {
                return;
            }
            streamCallback.changeOutputStream(outputStream); // this output stream would be closed when streamCallback are finished

            // wait 30 minutes, every 1 minute, check if the project was finished
            for(int i = 0;i < 30;i++) {
                Thread.sleep(60*1000);
                streamCallback = StreamCache.getInstance().get(params.getSession());
                if(streamCallback == null) {
                    // finished, no need to wait, return directly
                    return;
                }
            }
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        return super.refresh(params);
    }

    private void notifyHistory(String session, OutputStream inputOutputStream) {
        try {
            innerNotifyHistory(session, inputOutputStream);
        }
        catch(Exception ex) {
        }
    }

    private void innerNotifyHistory(String session, OutputStream outputStream) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    StorageIFC storageIFC = StorageInDBImpl.getInstance(dbConnection);
                    List<AIModel.CodeRecord> codeRecords = storageIFC.getCodeRecords(session);
                    String information = "";
                    for(AIModel.CodeRecord codeRecord: codeRecords) {
                        if(codeRecord.getContent() == null || codeRecord.getContent().trim().equals("")) {
                            continue;
                        }
                        information += "\n\n" + codeRecord.getContent();
                    }
                    flushInformation(information, outputStream);
                }
                catch(Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return null;
            }
        });
    }

    private static void flushInformation(String information, OutputStream outputStream) throws Exception {
        if(information == null || information.trim().equals("")) {
            return;
        }
        String toFlush = "\n\n" + information;
        toFlush = toFlush.replace("\n", "<br>");
        outputStream.write(toFlush.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public static class StreamCache {
        private static StreamCache instance = new StreamCache();
        private StreamCache() {
        }

        public static StreamCache getInstance() {
            return instance; 
        }

        private Map<String, AICoderBot.StreamCallbackImpl> streamMap = new HashMap<String, AICoderBot.StreamCallbackImpl>();
        public void put(String session, AICoderBot.StreamCallbackImpl streamCallback) {
            streamMap.put(session, streamCallback);
        }

        public AICoderBot.StreamCallbackImpl get(String session) {
            if(streamMap.containsKey(session)) {
                return streamMap.get(session);
            }
            else {
                return null;
            }
        }

        public void remove(String session) {
            if(streamMap.containsKey(session)) {
                StreamCallbackImpl streamCallback = streamMap.get(session);
                streamCallback.closeOutputStream(); // make sure the output stream was closed to release resource
                streamMap.remove(session);
            } 
        }
    }

    public static class StreamCallbackImpl implements NotifyCallbackIFC {
        private WSModel.AIChatParams params;
        private OutputStream  outputStream;
        public StreamCallbackImpl(WSModel.AIChatParams inputParams, OutputStream inputOutputStream) {
            params = inputParams; 
            outputStream = inputOutputStream;

            AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(params.getSession());
            codeRecord.setCreateTime(new Date());
            codeRecord.setRequirement(params.getUserInput());
            saveCodeRecord(codeRecord);
        }

        public void changeOutputStream(OutputStream inputOutputStream) {
            closeOutputStream(); // close original output stream before switch to new one
            outputStream = inputOutputStream;
        }

        private void saveCodeRecord(AIModel.CodeRecord codeRecord) {
            try {
                innerSaveCodeRecord(codeRecord);
            }
            catch(Exception ex) {
            }
        }

        private void innerSaveCodeRecord(AIModel.CodeRecord codeRecord) {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            dbService.executeSaveTask(new DBSaveTaskIFC() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    StorageIFC storageIFC = StorageInDBImpl.getInstance(dbConnection);
                    storageIFC.addCodeRecord(codeRecord.getSession(), codeRecord);
                    return null;
                }
            });
        }

        public void closeOutputStream() {
            if(outputStream == null) {
                return;
            }
            try {
               outputStream.close();
            }
            catch(Exception ex) {
            }
        }


        @Override
        public void notify(String information) {
            try {
                AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(params.getSession());
                codeRecord.setCreateTime(new Date());
                codeRecord.setContent(information);
                saveCodeRecord(codeRecord);
                flushInformation(information, outputStream);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage());
            }
        }
    }
}
