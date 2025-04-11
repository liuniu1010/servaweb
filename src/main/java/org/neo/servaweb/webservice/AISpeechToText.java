package org.neo.servaweb.webservice;

import java.io.File;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.SpeechToTextInMemoryForUIImpl;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;

@Path("/aispeechtotext")
public class AISpeechToText extends AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AISpeechToText.class);
    final static String HOOK = "aispeechtotext";
    private String audiosFolder = "audios";

    protected String getOnlineFileAbsolutePath() {
        return super.getAbsoluteResourcePath() + File.separator + audiosFolder;
    }

    protected String getRelevantVisitPath() {
        return audiosFolder;
    }

    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return SpeechToTextInMemoryForUIImpl.getInstance(getOnlineFileAbsolutePath(), getRelevantVisitPath());
    }

    @Override
    protected String getHook() {
        return HOOK;
    }

    @POST
    @Path("/sendaudio")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse sendAudio(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getSession();
            checkAccessibilityOnClientAction(loginSession);
            return super.sendAudio(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }
}
