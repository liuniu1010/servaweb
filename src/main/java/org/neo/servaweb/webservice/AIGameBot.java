package org.neo.servaweb.webservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;
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
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.GameBotInMemoryForUIImpl;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;

@Path("/aigamebot")
public class AIGameBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AIGameBot.class);
    final static String HOOK = "aigamebot";
    private String audiosFolder = "audios";

    protected String getOnlineFileAbsolutePath() {
        return super.getAbsoluteResourcePath() + File.separator + audiosFolder;
    }

    protected String getRelevantVisitPath() {
        return audiosFolder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return GameBotInMemoryForUIImpl.getInstance(getOnlineFileAbsolutePath(), getRelevantVisitPath());
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
        AIGameBot.StreamCallbackImpl notifyCallback = null;
        String loginSession = params.getSession();
        String alignedSession = super.alignSession(loginSession);
        String userInput = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with input: " + userInput);
        try {
            checkAccessibilityOnClientAction(loginSession);
            outputStream = response.getOutputStream();

            // generate notifycall back and begin code generation 
            notifyCallback = new AIGameBot.StreamCallbackImpl(params, outputStream);
            StreamCache.getInstance().put(alignedSession, notifyCallback);
            notifyCallback.notify(params.getUserInput()); 
            super.streamsend(params, notifyCallback);
            consume(loginSession);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
        finally {
            // StreamCache.getInstance().remove(alignedSession); // this will close the associated outputstream
        }
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
            StreamCallbackImpl streamCallback = StreamCache.getInstance().get(alignedSession);
            if(streamCallback == null) {
                return;
            }
            streamCallback.changeOutputStream(outputStream); // this output stream would be closed when streamCallback are finished
            streamCallback.notifyHistory();

            // wait 5 minutes, every 1 minute, check if the project was finished
            for(int i = 0;i < 5;i++) {
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
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse send(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getSession();
            checkAccessibilityOnClientAction(loginSession);
            WSModel.AIChatResponse chatResponse = super.send(params);
            consume(loginSession);
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getSession();
            checkAccessibilityOnClientAction(loginSession);
            return super.echo(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getSession();
            checkAccessibilityOnClientAction(loginSession);
            String alignedSession = super.alignSession(loginSession);
            StreamCache.getInstance().remove(alignedSession);
            return super.newchat(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/previousstep")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse previousstep(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getSession();
            checkAccessibilityOnClientAction(loginSession);
            String alignedSession = super.alignSession(loginSession);
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            storageIFC.popCodeFeedback(alignedSession);
            AIModel.CodeFeedback codeFeedback = storageIFC.peekCodeFeedback(alignedSession);
            if(codeFeedback != null) {
                codeFeedback.setIndex(AIModel.CodeFeedback.INDEX_CODECONTENT);
            }

            return super.refresh(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getSession();
            checkAccessibilityOnClientAction(loginSession);
            return super.refresh(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    private static void flushInformation(String information, OutputStream outputStream) throws Exception {
        if(information == null || information.trim().equals("")) {
            return;
        }
        outputStream.write(information.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
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
                int consumedCreditsOnGameBot = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnGameBot");
                String consumeFunction = "gamebot";
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                accountAgent.consumeCreditsWithSession(dbConnection, loginSession, consumedCreditsOnGameBot, consumeFunction);
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

        private Map<String, AIGameBot.StreamCallbackImpl> streamMap = new ConcurrentHashMap<String, AIGameBot.StreamCallbackImpl>();    
        public void put(String alignedSession, AIGameBot.StreamCallbackImpl streamCallback) {
            if(streamCallback != null) {
                streamMap.put(alignedSession, streamCallback);
            }   
        }   

        public AIGameBot.StreamCallbackImpl get(String alignedSession) {
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
        }

        public void changeOutputStream(OutputStream inputOutputStream) {
            closeOutputStream(); // close original output stream before switch to new one
            outputStream = inputOutputStream;
        }

        public void notifyHistory() throws Exception {
            try {
                innerNotifyHistory();
            }
            catch(Exception ex) {
            }
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
                flushInformation(information, outputStream);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        private String alignSession(String loginSession) {
            return HOOK + loginSession;
        }

        private void innerNotifyHistory() throws Exception {
            String loginSession = params.getSession();
            String alignedSession = alignSession(loginSession);
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            AIModel.CodeFeedback codeFeedback = storageIFC.peekCodeFeedback(alignedSession);
            if(codeFeedback != null) {
                flushInformation(codeFeedback.toString(), outputStream);
            }
        }
    }
}
