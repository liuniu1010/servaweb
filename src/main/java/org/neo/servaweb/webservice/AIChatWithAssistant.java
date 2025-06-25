package org.neo.servaweb.webservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletResponse;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;
import org.neo.servaaiagent.impl.ChatWithAssistantInMemoryForUIImpl;

@Path("/aichatwithassistant")
public class AIChatWithAssistant extends AbsAIChat {
    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return ChatWithAssistantInMemoryForUIImpl.getInstance();
    }

    @Override
    protected String getHook() {
        return "aichatwithassistant";
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse send(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            checkAccessibilityOnClientAction(loginSession);
            WSModel.AIChatResponse chatResponse = super.send(params);
            consume(loginSession);
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            checkAccessibilityOnClientAction(loginSession);
            return super.echo(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            checkAccessibilityOnClientAction(loginSession);
            return super.newchat(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            checkAccessibilityOnClientAction(loginSession);
            return super.refresh(params);
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
                int consumedCreditsOnChatWithAssistant = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnChatWithAssistant");
                String consumeFunction = "chatwithassistant";
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                accountAgent.consumeCreditsWithSession(dbConnection, loginSession, consumedCreditsOnChatWithAssistant, consumeFunction);
                return null;
            }
        });
    }
}
