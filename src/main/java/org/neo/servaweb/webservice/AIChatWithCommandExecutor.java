package org.neo.servaweb.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.neo.servaweb.ifc.ChatForUIIFC;
import org.neo.servaweb.impl.ChatWithCommandExecutorForUIImpl;

@Path("/aichatwithcommandexecutor")
public class AIChatWithCommandExecutor extends AbsAIChat {
    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return ChatWithCommandExecutorForUIImpl.getInstance();
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse send(WSModel.AIChatParams params) {
        return super.send(params);
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
