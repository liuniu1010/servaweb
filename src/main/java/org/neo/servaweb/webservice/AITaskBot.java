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
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.impl.TaskBotInMemoryForUIImpl;
import org.neo.servaaiagent.impl.AccountAgentImpl;

@Path("/aitaskbot")
public class AITaskBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AITaskBot.class);
    final static String HOOK = "aitaskbot";

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return TaskBotInMemoryForUIImpl.getInstance();
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
        AITaskBot.StreamCallbackImpl notifyCallback = null;
        String loginSession = params.getSession();
        String alignedSession = super.alignSession(loginSession);
        String requirement = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with task requirement: " + requirement);
        try {
            checkAccessibilityOnAdminAction(loginSession);

            outputStream = response.getOutputStream();
            notifyCallback = new AITaskBot.StreamCallbackImpl(params, outputStream);
            notifyCallback.registerWorkingThread();
            AITaskBot.StreamCache.getInstance().put(alignedSession, notifyCallback);
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
            AITaskBot.StreamCache.getInstance().remove(alignedSession); // this will close the associated outputstream
        }
    }


    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(WSModel.AIChatParams params) {
        String loginSession = params.getSession();
        checkAccessibilityOnAdminAction(loginSession);
        return super.echo(params);
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        String loginSession = params.getSession();
        String alignedSession = super.alignSession(loginSession);
        logger.info("loginSession: " + loginSession + " try to newchat");
        try {
            checkAccessibilityOnAdminAction(loginSession);
            AITaskBot.StreamCallbackImpl streamCallback = AITaskBot.StreamCache.getInstance().get(alignedSession);
            if(streamCallback != null) {
                streamCallback.clearHistory();
            }
            AITaskBot.StreamCache.getInstance().remove(alignedSession);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
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
            checkAccessibilityOnAdminAction(loginSession);

            OutputStream outputStream = response.getOutputStream();
            AITaskBot.StreamCallbackImpl streamCallback = AITaskBot.StreamCache.getInstance().get(alignedSession);
            if(streamCallback == null) {
                return;
            }
            streamCallback.changeOutputStream(outputStream); // this output stream would be closed when streamCallback are finished
            streamCallback.notifyHistory();
            // wait 30 minutes, every 1 minute, check if the project was finished
            for(int i = 0;i < 30;i++) {
                Thread.sleep(60*1000);
                streamCallback = AITaskBot.StreamCache.getInstance().get(alignedSession);
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
        String loginSession = params.getSession();
        checkAccessibilityOnAdminAction(loginSession);
        return super.refresh(params);
    }

    public static class StreamCache {
        private static StreamCache instance = new StreamCache();
        private StreamCache() {
        }

        public static StreamCache getInstance() {
            return instance; 
        }

        private Map<String, AITaskBot.StreamCallbackImpl> streamMap = new ConcurrentHashMap<String, AITaskBot.StreamCallbackImpl>();
        public void put(String alignedSession, AITaskBot.StreamCallbackImpl streamCallback) {
            if(streamCallback != null) {
                streamMap.put(alignedSession, streamCallback);
            }
        }

        public AITaskBot.StreamCallbackImpl get(String alignedSession) {
            if(streamMap.containsKey(alignedSession)) {
                return streamMap.get(alignedSession);
            }
            else {
                return null;
            }
        }

        public void remove(String alignedSession) {
            if(streamMap.containsKey(alignedSession)) {
                AITaskBot.StreamCallbackImpl streamCallback = streamMap.get(alignedSession);
                streamCallback.removeWorkingThread();
                streamCallback.closeOutputStream(); // make sure the output stream was closed to release resource
                streamMap.remove(alignedSession);
            } 
        }
    }

    public static class StreamCallbackImpl implements NotifyCallbackIFC {
        private WSModel.AIChatParams params;
        private OutputStream  outputStream;
        private int workingThreadHashCode;  // to control only one thread has the ability to push data on it

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

        private void registerWorkingThread() {
            workingThreadHashCode = Thread.currentThread().hashCode();
        }   

        private void removeWorkingThread() {
            workingThreadHashCode = 0;
        }

        private boolean isWorkingThread() {
            return workingThreadHashCode == Thread.currentThread().hashCode();
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
                if(!isWorkingThread()) {
                    throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOT_WORKING_THREAD);
                }
                String loginSession = params.getSession();
                String alignedSession = this.alignSession(loginSession);
                AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(alignedSession);
                codeRecord.setCreateTime(new Date());
                codeRecord.setContent(information);
                saveCodeRecord(codeRecord);
                flushInformation(information, outputStream);
            }
            catch(NeoAIException nex) {
                throw nex;
            }
            catch(Exception ex) {
                logger.error(ex.getMessage());
            }
        }

        private void clearHistory() {
            try {
                innerClearHistory();
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
            }  
        }

        private void notifyHistory() {
            try {
                innerNotifyHistory();
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        private void innerNotifyHistory() throws Exception {
            String loginSession = params.getSession();
            String alignedSession = alignSession(loginSession); 
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

        private void innerClearHistory() {
            String loginSession = params.getSession();
            String alignedSession = alignSession(loginSession);
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            storageIFC.clearCodeRecords(alignedSession);
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
    }
}
