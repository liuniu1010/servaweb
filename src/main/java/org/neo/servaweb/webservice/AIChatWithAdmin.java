package org.neo.servaweb.webservice;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.http.HttpServletResponse;



import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.impl.ChatWithAdminInMemoryForUIImpl;

@Path("/aichatwithadmin")
public class AIChatWithAdmin extends AbsAIChat {
    @Override
    protected ChatForUIIFC getChatForUIInstance() {
        return ChatWithAdminInMemoryForUIImpl.getInstance();
    }

    @Override
    protected String getHook() {
        return "aichatwithadmin";
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse send(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            checkAccessibilityOnAdminAction(loginSession);
            return super.send(params);
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
            checkAccessibilityOnAdminAction(loginSession);
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
            checkAccessibilityOnAdminAction(loginSession);
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
            checkAccessibilityOnAdminAction(loginSession);
            return super.refresh(params);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }
}
