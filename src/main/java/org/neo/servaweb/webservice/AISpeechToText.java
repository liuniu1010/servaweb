package org.neo.servaweb.webservice;

import java.io.File;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.http.HttpServletResponse;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.SpeechToTextInMemoryForUIImpl;

@Path("/aispeechtotext")
public class AISpeechToText extends AbsAIChat {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AISpeechToText.class);
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
            String loginSession = params.getLoginSession();
            checkAccessibilityOnClientAction(loginSession);
            WSModel.AIChatResponse chatResponse = super.sendAudio(params);
            consume(loginSession);
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
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
                int consumedCreditsOnSpeechToText = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnSpeechToText"); 
                String consumeFunction = "speechToText";
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                accountAgent.consumeCreditsWithSession(dbConnection, loginSession, consumedCreditsOnSpeechToText, consumeFunction);
                return null;
            }
        });
    }
}
