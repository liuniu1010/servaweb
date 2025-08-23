package org.neo.servaweb.webservice;

import java.io.File;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.ChatWithSpeechTranslateForUIImpl;

@Path("/speechtranslate")
public class AIChatWithSpeechTranslateExpert extends AbsAIChat {
    private String audiosFolder = "audios";

    protected String getOnlineFileAbsolutePath() {
        return super.getAbsoluteResourcePath() + File.separator + audiosFolder;
    }

    protected String getRelevantVisitPath() {
        return audiosFolder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return ChatWithSpeechTranslateForUIImpl.getInstance(getOnlineFileAbsolutePath(), getRelevantVisitPath());
    }

    @Override
    protected String getHook() {
        return "speechtranslate";
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
