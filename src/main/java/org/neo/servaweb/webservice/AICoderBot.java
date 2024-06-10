package org.neo.servaweb.webservice;

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

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            NotifyCallbackIFC notifyCallback = new AICoderBot.StreamCallbackImpl(outputStream);
            super.streamsend(params, notifyCallback);
        }
        catch (Exception ex) {
        }
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
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        return super.refresh(params);
    }

    public static class StreamCallbackImpl implements NotifyCallbackIFC {
        OutputStream  outputStream;
        public StreamCallbackImpl(OutputStream inputOutputStream) {
            outputStream = inputOutputStream;
        }

        @Override
        public void notify(String information) {
            try {
                logger.info("notify: " + information);
                String toFlush = "\n\n" + information;
                toFlush = toFlush.replace("\n", "<br>");
                outputStream.write(toFlush.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
