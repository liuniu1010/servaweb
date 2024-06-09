package org.neo.servaweb.webservice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import java.nio.charset.StandardCharsets;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.CoderBotForUIImpl;

@Path("/coderbot")
public class AICoderBot extends AbsAIChat {
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
    public Response stream() {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException {
                try {
                    for (int i = 1; i <= 10; i++) {
                        String data = "data: Step " + i + " is in progress\n\n";
                        os.write(data.getBytes(StandardCharsets.UTF_8));
                        os.flush();  // Ensure the data is sent immediately
                        Thread.sleep(1000);
                    }
                    String data = "data: Process completed\n\n";
                    os.write(data.getBytes(StandardCharsets.UTF_8));
                    os.flush();  // Ensure the data is sent immediately
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return Response.ok(stream)
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .build();
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
}
