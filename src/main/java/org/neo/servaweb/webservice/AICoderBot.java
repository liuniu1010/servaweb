package org.neo.servaweb.webservice;

import java.util.Map;
import java.util.HashMap;

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

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.CoderBotForUIImpl;

@Path("/aicoderbot")
public class AICoderBot extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AICoderBot.class);
    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return CoderBotForUIImpl.getInstance();
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
            outputStream = response.getOutputStream();
            notifyCallback = new AICoderBot.StreamCallbackImpl(outputStream);
            StreamCache.getInstance().put(params.getSession(), notifyCallback);
            // super.streamsend(params, notifyCallback);
            virtualStreamsend(notifyCallback);
        }
        catch (Exception ex) {
        }
        finally {
            StreamCache.getInstance().remove(params.getSession()); // this will close the associated outputstream
        }
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
        StreamCallbackImpl streamCallback = StreamCache.getInstance().get(params.getSession());
        if(streamCallback == null) {
            return;
        }

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            streamCallback.setOutputStream(outputStream); // this output stream would be closed when streamCallback are finished
        }
        catch(Exception ex) {
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        return super.refresh(params);
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
        OutputStream  outputStream;
        public StreamCallbackImpl(OutputStream inputOutputStream) {
            outputStream = inputOutputStream;
        }

        public void setOutputStream(OutputStream inputOutputStream) {
            logger.info("set new outputstream");
            closeOutputStream(); // close original output stream before switch to new one
            logger.info("origin outputstream closed");
            outputStream = inputOutputStream;
            logger.info("new outputstream setted");
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
                logger.info("notify: " + information);
                String toFlush = "\n\n" + information;
                toFlush = toFlush.replace("\n", "<br>");
                outputStream.write(toFlush.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                logger.info("flush success");
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
