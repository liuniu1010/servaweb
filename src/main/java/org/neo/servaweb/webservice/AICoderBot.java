package org.neo.servaweb.webservice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

import java.io.File;
import java.io.OutputStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
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

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.impl.CoderBotInMemoryForUIImpl;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.SimpleNotifyCallbackImpl;

import org.neo.servaweb.util.StreamCache;

@Path("/aicoderbot")
public class AICoderBot extends AbsAIChat {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AICoderBot.class);
    final static String START_MARK = "-----start-----";
    final static String END_MARK = "-----end-----";
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
    public void streamsend(@Context HttpServletRequest request, @Context HttpServletResponse response, final WSModel.AIChatParams params) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        NotifyCallbackIFC notifyCallback = null;
        String loginSession = params.getLoginSession();
        String alignedSession = super.alignSession(loginSession);
        String requirement = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with requirement: " + requirement);
        try {
            checkAccessibilityOnClientAction(loginSession);
            
            // get or create new notifycallback
            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(0); // let you manage the timeout

            OutputStream outputStream = asyncContext.getResponse().getOutputStream();

            notifyCallback = StreamCache.getInstance().get(alignedSession);
            if(notifyCallback == null) {
                notifyCallback = new SimpleNotifyCallbackImpl(outputStream);
            }
            else {
                notifyCallback.changeOutputStream(outputStream);
            }
            notifyCallback.registerWorkingThread();
            notifyCallback.notifyHistory();
            notifyCallback.notify("<br>" + START_MARK);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            final NotifyCallbackIFC streamCallback = notifyCallback;
            Future<?> future = executor.submit(() -> {
                inThreadStreamSend(asyncContext, params, streamCallback);
            });

            StreamCache.getInstance().put(alignedSession, notifyCallback, future);
        }
        catch(Exception ex) {
            logger.warn(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
        finally {
            // StreamCache.getInstance().remove(alignedSession); // this will close the associated outputstream
        }
    }

    private void inThreadStreamSend(AsyncContext asyncContext, WSModel.AIChatParams params, NotifyCallbackIFC notifyCallback) {
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        try {
            notifyCallback.registerWorkingThread();
            String loginSession = params.getLoginSession();
            WSModel.AIChatResponse chatResponse = super.streamsend(params, notifyCallback);
            String information = "";
            if(chatResponse.getIsSuccess()) {
                information = "Code generated success, " + chatResponse.getMessage();
                consume(loginSession);
            }
            else {
                information = "Failed to generate code due to: " + chatResponse.getMessage();
            }

            if(notifyCallback.isWorkingThread()) {
                notifyCallback.notify("<br>" + CommonUtil.renderToShowAsHtml(information));
            }
        }
        catch(Exception ex) {
            logger.warn(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
        finally {
            if(notifyCallback.isWorkingThread()) {
                notifyCallback.notify("<br>" + END_MARK);
            }
            asyncContext.complete();
        }
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        checkAccessibilityOnClientAction(loginSession);
        return super.echo(params);
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
            checkAccessibilityOnClientAction(loginSession);
            StreamCache.getInstance().remove(alignedSession);
        }
        catch(Exception ex) {
            logger.warn(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
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
            checkAccessibilityOnClientAction(loginSession);

            NotifyCallbackIFC notifyCallback = StreamCache.getInstance().get(alignedSession);

            if (notifyCallback == null) {
                response.getOutputStream().write(" ".getBytes(StandardCharsets.UTF_8));
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
        catch (Exception ex) {
            logger.warn(ex.getMessage());
            standardHandleException(ex, response);
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
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

    private void consume(String loginSession) {
        try {
            innerConsume(loginSession);
        }
        catch(Exception ex) {
            logger.warn(ex.getMessage(), ex);
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
}
