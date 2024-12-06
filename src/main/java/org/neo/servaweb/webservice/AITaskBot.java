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
import javax.servlet.http.HttpServletRequest;
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
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.TaskBotInMemoryForUIImpl;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;

@Path("/aitaskbot")
public class AITaskBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AITaskBot.class);

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return TaskBotInMemoryForUIImpl.getInstance();
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
        String requirement = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with task requirement: " + requirement);
        try {
            checkAccessibilityOnStreamsend(loginSession);

            outputStream = response.getOutputStream();
            notifyHistory(params.getSession(), outputStream);
            notifyCallback = new AITaskBot.StreamCallbackImpl(params, outputStream);
            StreamCache.getInstance().put(params.getSession(), notifyCallback);
            WSModel.AIChatResponse chatResponse = super.streamsend(params, notifyCallback);

            String information = "";
            if(chatResponse.getIsSuccess()) {
                information = "task executed success, " + chatResponse.getMessage();
            }
            else {
                information = "Failed to execute task due to: " + chatResponse.getMessage();
            }
            notifyCallback.notify(information);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
        finally {
            StreamCache.getInstance().remove(params.getSession()); // this will close the associated outputstream
        }
    }

    private void standardHandleException(Exception ex, HttpServletResponse response) {
        terminateConnection(decideHttpResponseStatus(ex), ex.getMessage(), response);
    }

    private int decideHttpResponseStatus(Exception ex) {
        if(ex instanceof NeoAIException) {
            NeoAIException nex = (NeoAIException)ex;
            if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_SESSION_INVALID
                || nex.getCode() == NeoAIException.NEOAIEXCEPTION_LOGIN_FAIL) {
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
            else if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_NOCREDITS_LEFT) {
                return HttpServletResponse.SC_PAYMENT_REQUIRED;
            }
        }
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private void terminateConnection(int httpStatus, String message, HttpServletResponse response) {
        try {
            response.setStatus(httpStatus);
            response.getWriter().write(message);
            response.flushBuffer();
            return;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void checkAccessibilityOnStreamsend(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnStreamsend(dbConnection, loginSession);
                }
                catch(NeoAIException nex) {
                    throw nex;
                }
                catch(Exception ex) {
                    throw new NeoAIException(ex.getMessage(), ex);
                }
                return null;
            }
        }); 
    }

    private void innerCheckAccessibilityOnStreamsend(DBConnectionIFC dbConnection, String loginSession) {
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        accessAgent.verifyMaintenance(dbConnection);

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.checkSessionValid(dbConnection, loginSession);
        accountAgent.updateSession(dbConnection, loginSession);
        accountAgent.checkCreditsWithSession(dbConnection, loginSession);
    }

    private void checkAccessibilityOnStreamrefresh(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnStreamrefresh(dbConnection, loginSession);
                }
                catch(NeoAIException nex) {
                    throw nex;
                }
                catch(Exception ex) {
                    throw new NeoAIException(ex.getMessage(), ex);
                }
                return null;
            }
        }); 
    }

    private void innerCheckAccessibilityOnStreamrefresh(DBConnectionIFC dbConnection, String loginSession) {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.checkSessionValid(dbConnection, loginSession);
        accountAgent.updateSession(dbConnection, loginSession);
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

        String loginSession = params.getSession();
        logger.info("loginSession: " + loginSession + " try to streamrefresh");
        try {
            checkAccessibilityOnStreamrefresh(loginSession);

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
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        return super.refresh(params);
    }

    private void notifyHistory(String loginSession, OutputStream inputOutputStream) {
        try {
            // innerNotifyHistoryFromDB(loginSession, inputOutputStream);
            innerNotifyHistoryFromMemory(loginSession, inputOutputStream);
        }
        catch(Exception ex) {
        }
    }

    private void innerNotifyHistoryFromDB(String loginSession, OutputStream outputStream) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    StorageIFC storageIFC = StorageInDBImpl.getInstance(dbConnection);
                    List<AIModel.CodeRecord> codeRecords = storageIFC.getCodeRecords(loginSession);
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

    private void innerNotifyHistoryFromMemory(String loginSession, OutputStream outputStream) {
        try {
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            List<AIModel.CodeRecord> codeRecords = storageIFC.getCodeRecords(loginSession);
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

    public static class StreamCache {
        private static StreamCache instance = new StreamCache();
        private StreamCache() {
        }

        public static StreamCache getInstance() {
            return instance; 
        }

        private Map<String, AITaskBot.StreamCallbackImpl> streamMap = new HashMap<String, AITaskBot.StreamCallbackImpl>();
        public void put(String loginSession, AITaskBot.StreamCallbackImpl streamCallback) {
            streamMap.put(loginSession, streamCallback);
        }

        public AITaskBot.StreamCallbackImpl get(String loginSession) {
            if(streamMap.containsKey(loginSession)) {
                return streamMap.get(loginSession);
            }
            else {
                return null;
            }
        }

        public void remove(String loginSession) {
            if(streamMap.containsKey(loginSession)) {
                StreamCallbackImpl streamCallback = streamMap.get(loginSession);
                streamCallback.closeOutputStream(); // make sure the output stream was closed to release resource
                streamMap.remove(loginSession);
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
