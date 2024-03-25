package org.neo.servaweb.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.neo.servaweb.ifc.ChatForUIIFC;
import org.neo.servaweb.impl.ChatForUIImpl;

@Path("/aichat")
public class AIChatResource {
    final static Logger logger = Logger.getLogger(AIChatResource.class);

    private ChatForUIIFC getChatForUIInstance() {
        return ChatForUIImpl.getInstance();
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AIChatResponse send(AIChatParams params) {
        try {
            String session = params.getSession();
            String userInput = params.getUserInput();
            String renderedResponse = getChatForUIInstance().fetchResponse(session, userInput);

            AIChatResponse response = new AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new AIChatResponse(false, ex.getMessage());
        }
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AIChatResponse echo(AIChatParams params) {
        try {
            String session = params.getSession();
            String userInput = params.getUserInput();
            String renderedResponse = getChatForUIInstance().echo(session, userInput);

            AIChatResponse response = new AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new AIChatResponse(false, ex.getMessage());
        }
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AIChatResponse newchat(AIChatParams params) {
        try {
            String session = params.getSession();
            String renderedResponse = getChatForUIInstance().initNewChat(session);

            AIChatResponse response = new AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new AIChatResponse(false, ex.getMessage());
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AIChatResponse refresh(AIChatParams params) {
        try {
            String session = params.getSession();
            String renderedResponse = getChatForUIInstance().refresh(session);

            AIChatResponse response = new AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new AIChatResponse(false, ex.getMessage());
        }
    }
}

class AIChatParams {
    String session;
    String userInput;

    public String getSession() {
        return session;
    }

    public void setSession(String inputSession) {
        session = inputSession;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String inputUserInput) {
        userInput = inputUserInput;
    }
}

class AIChatResponse {
    private boolean isSuccess;
    private String message;

    public AIChatResponse(boolean inputIsSuccess, String inputMessage) {
        isSuccess = inputIsSuccess;
        message = inputMessage;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean inputIsSuccess) {
        isSuccess = inputIsSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String inputMessage) {
        message = inputMessage;
    } 
}
