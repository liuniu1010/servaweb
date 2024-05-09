package org.neo.servaweb.webservice;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.ChatWithSpeechExpertForUIImpl;

@Path("/aichatwithspeechexpert")
public class AIChatWithSpeechExpert extends AbsAIChat {
    private String mp3Folder = "mp3";

    protected String getOnlineFileMountPoint() {
        return super.getAbsoluteResourcePath() + File.separator + mp3Folder;
    }

    protected String getRelavantVisitPath() {
        return mp3Folder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return ChatWithSpeechExpertForUIImpl.getInstance(getOnlineFileMountPoint(), getRelavantVisitPath());
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
