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

import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.impl.TaskBotInMemoryForUIImpl;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.SimpleNotifyCallbackImpl;

@Path("/aitaskbot")
public class AITaskBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AITaskBot.class);
    final static String START_MARK = "-----start-----";
    final static String END_MARK = "-----end-----";
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

        NotifyCallbackIFC notifyCallback = null;
        String loginSession = params.getSession();
        String alignedSession = super.alignSession(loginSession);
        String requirement = params.getUserInput();
        logger.info("loginSession: " + loginSession + " try to streamsend with task requirement: " + requirement);
        try {
            checkAccessibilityOnAdminAction(loginSession);

            // get or create new notifycallback
            OutputStream outputStream = response.getOutputStream();
            notifyCallback = StreamCache.getInstance().get(alignedSession);
            if(notifyCallback == null) {
                notifyCallback = new SimpleNotifyCallbackImpl(outputStream);
                StreamCache.getInstance().put(alignedSession, notifyCallback);
            }
            else {
                notifyCallback.changeOutputStream(outputStream);
            }
            notifyCallback.registerWorkingThread();
            notifyCallback.notifyHistory();
            notifyCallback.notify("<br>" + START_MARK);

            WSModel.AIChatResponse chatResponse = super.streamsend(params, notifyCallback);

            String information = "";
            if(chatResponse.getIsSuccess()) {
                information = "task executed success, " + chatResponse.getMessage();
            }
            else {
                information = "Failed to execute task due to: " + chatResponse.getMessage();
            }
            if(notifyCallback.isWorkingThread()) {
                notifyCallback.notify("<br>" + information);
            }
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            standardHandleException(ex, response);
        }
        finally {
            // AITaskBot.StreamCache.getInstance().remove(alignedSession); // this will close the associated outputstream
            if(notifyCallback != null && notifyCallback.isWorkingThread()) {
                notifyCallback.notify("<br>" + END_MARK);
            }
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
            StreamCache.getInstance().remove(alignedSession);
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

            // get or create new notifycallback
            OutputStream outputStream = response.getOutputStream();
            NotifyCallbackIFC notifyCallback = StreamCache.getInstance().get(alignedSession);
            if(notifyCallback == null) {
                return;
            }
            else {
                notifyCallback.changeOutputStream(outputStream);
            }
            notifyCallback.notifyHistory();

            // wait 5 minutes, every 1 minute, check if the project was finished
            for(int i = 0;i < 5;i++) {
                Thread.sleep(60*1000);
                notifyCallback = AITaskBot.StreamCache.getInstance().get(alignedSession);
                if(notifyCallback == null) {
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

        private Map<String, NotifyCallbackIFC> streamMap = new ConcurrentHashMap<String, NotifyCallbackIFC>();

        public void put(String alignedSession, NotifyCallbackIFC notifyCallback) {
            if(notifyCallback != null) {
                streamMap.put(alignedSession, notifyCallback);
            }
        }

        public NotifyCallbackIFC get(String alignedSession) {
            if(streamMap.containsKey(alignedSession)) {
                return streamMap.get(alignedSession);
            }
            else {
                return null;
            }
        }

        public void remove(String alignedSession) {
            if(streamMap.containsKey(alignedSession)) {
                NotifyCallbackIFC notifyCallback = streamMap.get(alignedSession);
                notifyCallback.removeWorkingThread();
                notifyCallback.clearHistory();
                notifyCallback.closeOutputStream(); // make sure the output stream was closed to release resource
                streamMap.remove(alignedSession);
            } 
        }
    }
}
