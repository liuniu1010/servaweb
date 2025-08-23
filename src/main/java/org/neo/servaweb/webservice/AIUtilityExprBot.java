package org.neo.servaweb.webservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;
import java.io.OutputStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
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
import org.neo.servaaiagent.impl.UtilityBotInMemoryForUIImpl;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.SimpleNotifyCallbackImpl;

import org.neo.servaweb.util.StreamCache;

@Path("/aiutilityexprbot")
public class AIUtilityExprBot extends AbsAIChat {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AIUtilityExprBot.class);
    final static String HOOK = "aiutilityexprbot";
    private String audiosFolder = "audios";

    private final static String ENDOFINPUT = "*****ENDOFINPUT*****";
    private final static String ENDOFCODE = "*****ENDOFCODE*****";

    protected String getOnlineFileAbsolutePath() {
        return super.getAbsoluteResourcePath() + File.separator + audiosFolder;
    }

    protected String getRelevantVisitPath() {
        return audiosFolder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return UtilityBotInMemoryForUIImpl.getInstance(getOnlineFileAbsolutePath(), getRelevantVisitPath());
    }

    @Override
    protected String getHook() {
        return HOOK;
    }

    @POST
    @Path("/streamsend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamsend(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        NotifyCallbackIFC notifyCallback = null;
        String loginSession = params.getLoginSession();
        String alignedSession = super.alignSession(loginSession);
        String userInput = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with input: " + userInput);
        try {
            checkAccessibilityOnExprTest(loginSession);
            if(!super.isBase64SizeValid(params.getFileAsBase64())) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_FILESIZE_EXCEED_UPPERLIMIT);
            }

            // get or create new notifycallback
            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(0);

            OutputStream outputStream = asyncContext.getResponse().getOutputStream();

            notifyCallback = StreamCache.getInstance().get(alignedSession);
            if(notifyCallback == null) {
                notifyCallback = new SimpleNotifyCallbackImpl(outputStream);
            }
            else {
                notifyCallback.changeOutputStream(outputStream);
            }
            notifyCallback.registerWorkingThread();
            notifyCallback.clearHistory();
            notifyCallback.notify(params.getUserInput() + ENDOFINPUT); 

            ExecutorService executor = Executors.newSingleThreadExecutor();
            final NotifyCallbackIFC streamCallback = notifyCallback;
            Future<?> future = executor.submit(() -> {
                inThreadStreamSend(asyncContext, params, streamCallback);
            });

            StreamCache.getInstance().put(alignedSession, notifyCallback, future);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
    }

    private void inThreadStreamSend(AsyncContext asyncContext, WSModel.AIChatParams params, NotifyCallbackIFC notifyCallback) {
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        try {
            notifyCallback.registerWorkingThread();
            String loginSession = params.getLoginSession();
            WSModel.AIChatResponse wsChatResponse = super.streamsend(params, notifyCallback);
            if(wsChatResponse.getIsSuccess()) {
                String fileAsBase64 = params.getFileAsBase64();
                if(fileAsBase64 == null || fileAsBase64.trim().equals("")) {
                    // temp logic, in case there is no file attached, it will trigger calling on LLM
                    consume(loginSession);
                }
            }
            else {
                notifyCallback.notify(wsChatResponse.getMessage() + ENDOFCODE);
            }
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            notifyCallback.notify(ex.getMessage() + ENDOFCODE);
            standardHandleException(ex, response);
        }
        finally {
            asyncContext.complete();
        }
    }

    @POST
    @Path("/streamrefresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamrefresh(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        String loginSession = params.getLoginSession();
        String alignedSession = super.alignSession(loginSession);
        logger.info("loginSession: " + loginSession + " try to streamrefresh");
        try {
            checkAccessibilityOnExprTest(loginSession);

            NotifyCallbackIFC notifyCallback = StreamCache.getInstance().get(alignedSession);

            if (notifyCallback == null) {
                response.getOutputStream().write((ENDOFCODE + " " + ENDOFCODE).getBytes(StandardCharsets.UTF_8));
                response.getOutputStream().flush();
                return;
            }

            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(0); // disable timeout, let us manage it

            OutputStream outputStream = asyncContext.getResponse().getOutputStream();
            notifyCallback.changeOutputStream(outputStream); // rebind new output stream
            notifyCallback.notifyHistory();

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (!StreamCache.getInstance().isTaskRunning(alignedSession)) {
                        asyncContext.complete();
                        scheduler.shutdown();
                    }
                }
                catch (Exception e) {
                    logger.warn("Exception in async streamrefresh check", e);
                    try {
                        asyncContext.complete();
                    }
                    catch (Exception ignored) {
                    }
                    scheduler.shutdown();
                }
            }, 0, 10, TimeUnit.SECONDS);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        String alignedSession = super.alignSession(loginSession);
        logger.info("loginSession: " + loginSession + " try to newchat");
        try {
            checkAccessibilityOnExprTest(loginSession);
            StreamCache.getInstance().remove(alignedSession);
            super.newchat(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/undo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void undo(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            checkAccessibilityOnExprTest(loginSession);

            String alignedSession = super.alignSession(loginSession);
            StorageIFC storageIFC = StorageInMemoryImpl.getInstance();
            storageIFC.popCodeFeedback(alignedSession);

            AIModel.CodeFeedback codeFeedback = storageIFC.peekCodeFeedback(alignedSession);
            NotifyCallbackIFC notifyCallback = StreamCache.getInstance().get(alignedSession);

            if(notifyCallback != null) {
                notifyCallback.registerWorkingThread();
                OutputStream outputStream = response.getOutputStream();
                notifyCallback.changeOutputStream(outputStream);
                notifyCallback.clearHistory();
                if(codeFeedback != null) {
                    notifyCallback.notify(codeFeedback.getCodeContent() + ENDOFCODE);
                }
                else {
                    notifyCallback.notify(ENDOFCODE + " " + ENDOFCODE);
                }
            }
            else {
                response.getOutputStream().write((ENDOFCODE + " " + ENDOFCODE).getBytes(StandardCharsets.UTF_8));
                response.getOutputStream().flush();
            }
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
    }

    private void checkAccessibilityOnExprTest(String loginSession) {
        int iValue = CommonUtil.getConfigValueAsInt("blockExprTest");
        if(iValue != 0) {
            throw new NeoAIException("expr test blocked!");
        }

        checkAccessibilityOnClientAction(loginSession);
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
                int consumedCreditsOnUtilityBot = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnUtilityBot");
                String consumeFunction = "utilitybot";
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                accountAgent.consumeCreditsWithSession(dbConnection, loginSession, consumedCreditsOnUtilityBot, consumeFunction);
                return null;
            }
        });
    }
}
