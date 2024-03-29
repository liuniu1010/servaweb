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
import org.neo.servaweb.impl.ChatWithBotForUIImpl;

@Path("/aichatwithbot")
public class AIChatWithBot {
    final static Logger logger = Logger.getLogger(AIChatWithBot.class);

    private ChatForUIIFC getChatForUIInstance() {
        return ChatWithBotForUIImpl.getInstance();
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse send(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String userInput = params.getUserInput();
            String renderedResponse = getChatForUIInstance().fetchResponse(session, userInput);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse echo(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String userInput = params.getUserInput();
            String renderedResponse = getChatForUIInstance().echo(session, userInput);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String renderedResponse = getChatForUIInstance().initNewChat(session);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse refresh(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String renderedResponse = getChatForUIInstance().refresh(session);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }
}
