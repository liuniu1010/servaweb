package org.neo.servaweb.webservice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

import java.io.File;
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
import org.neo.servaframe.interfaces.DBAutoCommitSaveTaskIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.impl.CoderBotInMemoryForUIImpl;
import org.neo.servaaiagent.impl.AccountAgentImpl;

@Path("/aicoderbot")
public class AICoderBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AICoderBot.class);
    final static String HOOK = "aicoderbot";

    private String downloadFolder = "download";

    protected String getOnlineFileAbsolutePath() {
        return super.getAbsoluteResourcePath() + File.separator + downloadFolder;
    }

    protected String getRelevantVisitPath() {
        return downloadFolder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return CoderBotInMemoryForUIImpl.getInstance(getOnlineFileAbsolutePath(), getRelevantVisitPath());
    }

    @Override
    protected String getHook() {
        return HOOK;
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
        String loginSession = params.getSession();
        String alignedSession = super.alignSession(loginSession);
        String requirement = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with requirement: " + requirement);
        try {
            checkAccessibilityOnClientAction(loginSession);

            outputStream = response.getOutputStream();
            notifyHistory(alignedSession, outputStream);
            notifyCallback = new AICoderBot.StreamCallbackImpl(params, outputStream);
            StreamCache.getInstance().put(alignedSession, notifyCallback);
            WSModel.AIChatResponse chatResponse = super.streamsend(params, notifyCallback);

            String information = "";
            if(chatResponse.getIsSuccess()) {
                information = "Code generated success, " + chatResponse.getMessage();
                consume(loginSession);
            }
            else {
                information = "Failed to generate code due to: " + chatResponse.getMessage();
            }
            notifyCallback.notify(information);
            // virtualStreamsend(notifyCallback);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
	catch(Error er) {
	    logger.error(er.getMessage(), er);
	    throw er;
	}
	catch(Throwable th) {
	    logger.error(th.getMessage(), th);
	    throw th;
	}
        finally {
            StreamCache.getInstance().remove(alignedSession); // this will close the associated outputstream
        }
    }


    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(WSModel.AIChatParams params) {
        String loginSession = params.getSession();
        checkAccessibilityOnClientAction(loginSession);
        return super.echo(params);
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(WSModel.AIChatParams params) {
        String loginSession = params.getSession();
        checkAccessibilityOnClientAction(loginSession);
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

        String loginSession = params.getSession();
        String alignedSession = super.alignSession(loginSession);
        logger.info("loginSession: " + loginSession + " try to streamrefresh");
        try {
            checkAccessibilityOnClientAction(loginSession);

            OutputStream outputStream = response.getOutputStream();
            notifyHistory(alignedSession, outputStream);
            StreamCallbackImpl streamCallback = StreamCache.getInstance().get(alignedSession);
            if(streamCallback == null) {
                return;
            }
            streamCallback.changeOutputStream(outputStream); // this output stream would be closed when streamCallback are finished

            // wait 30 minutes, every 1 minute, check if the project was finished
            for(int i = 0;i < 30;i++) {
                Thread.sleep(60*1000);
                streamCallback = StreamCache.getInstance().get(alignedSession);
                if(streamCallback == null) {
                    // finished, no need to wait, return directly
                    return;
                }
            }
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
	catch(Error er) {
	    logger.error(er.getMessage(), er);
	    throw er;
	}
	catch(Throwable th) {
	    logger.error(th.getMessage(), th);
	    throw th;
	}
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        String loginSession = params.getSession();
        checkAccessibilityOnClientAction(loginSession);
        return super.refresh(params);
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
    private void notifyHistory(String alignedSession, OutputStream inputOutputStream) {
        try {
            // innerNotifyHistoryFromDB(alignedSession, inputOutputStream);
            innerNotifyHistoryFromMemory(alignedSession, inputOutputStream);
        }
        catch(Exception ex) {
        }
    }

    private void innerNotifyHistoryFromDB(String alignedSession, OutputStream outputStream) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    StorageIFC storageIFC = StorageInDBImpl.getInstance(dbConnection);
                    List<AIModel.CodeRecord> codeRecords = storageIFC.getCodeRecords(alignedSession);
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

    private void innerNotifyHistoryFromMemory(String alignedSession, OutputStream outputStream) {
        try {
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            List<AIModel.CodeRecord> codeRecords = storageIFC.getCodeRecords(alignedSession);
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

    private void consume(String loginSession) {
        try {
            innerConsume(loginSession);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void innerConsume(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                int consumedCreditsOnCoderBot = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnCoderBot");
                String consumeFunction = "coderbot";
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                accountAgent.consumeCreditsWithSession(dbConnection, loginSession, consumedCreditsOnCoderBot, consumeFunction);
                return null;
            }
        });
    }

    public static class StreamCache {
        private static StreamCache instance = new StreamCache();
        private StreamCache() {
        }

        public static StreamCache getInstance() {
            return instance; 
        }

        private Map<String, AICoderBot.StreamCallbackImpl> streamMap = new ConcurrentHashMap<String, AICoderBot.StreamCallbackImpl>();
        public void put(String alignedSession, AICoderBot.StreamCallbackImpl streamCallback) {
            if(streamCallback != null) {
                streamMap.put(alignedSession, streamCallback);
            }
        }

        public AICoderBot.StreamCallbackImpl get(String alignedSession) {
            if(streamMap.containsKey(alignedSession)) {
                return streamMap.get(alignedSession);
            }
            else {
                return null;
            }
        }

        public void remove(String alignedSession) {
            if(streamMap.containsKey(alignedSession)) {
                StreamCallbackImpl streamCallback = streamMap.get(alignedSession);
                streamCallback.closeOutputStream(); // make sure the output stream was closed to release resource
                streamMap.remove(alignedSession);
            } 
        }
    }

    public static class StreamCallbackImpl implements NotifyCallbackIFC {
        private WSModel.AIChatParams params;
        private OutputStream  outputStream;
        public StreamCallbackImpl(WSModel.AIChatParams inputParams, OutputStream inputOutputStream) {
            params = inputParams; 
            outputStream = inputOutputStream;

            String loginSession = params.getSession();
            String alignedSession = this.alignSession(loginSession);
            AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(alignedSession);
            codeRecord.setCreateTime(new Date());
            codeRecord.setRequirement(params.getUserInput());
            saveCodeRecord(codeRecord);
        }

        public void changeOutputStream(OutputStream inputOutputStream) {
            closeOutputStream(); // close original output stream before switch to new one
            outputStream = inputOutputStream;
        }

        private String alignSession(String loginSession) {
            return HOOK + loginSession;
        }

        private void saveCodeRecord(AIModel.CodeRecord codeRecord) {
            try {
                // innerSaveCodeRecordInDB(codeRecord);
                innerSaveCodeRecordInMemory(codeRecord);
            }
            catch(Exception ex) {
            }
        }

        private void innerSaveCodeRecordInMemory(AIModel.CodeRecord codeRecord) {
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            storageIFC.addCodeRecord(codeRecord.getSession(), codeRecord);
        }

        private void innerSaveCodeRecordInDB(AIModel.CodeRecord codeRecord) {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            dbService.executeAutoCommitSaveTask(new DBAutoCommitSaveTaskIFC() {
                @Override
                public Object autoCommitSave(DBConnectionIFC dbConnection) {
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
                String loginSession = params.getSession();
                String alignedSession = this.alignSession(loginSession);
                AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(alignedSession);
                codeRecord.setCreateTime(new Date());
                codeRecord.setContent(information);
                saveCodeRecord(codeRecord);
                flushInformation(information, outputStream);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage());
            }
	    catch(Error er) {
	        logger.error(er.getMessage(), er);
	        throw er;
	    }
	    catch(Throwable th) {
	        logger.error(th.getMessage(), th);
	        throw th;
	    }
        }
    }
}
