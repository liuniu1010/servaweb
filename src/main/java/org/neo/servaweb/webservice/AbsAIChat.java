package org.neo.servaweb.webservice;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.neo.servaaiagent.ifc.ChatForUIIFC;

abstract public class AbsAIChat {
    final static Logger logger = Logger.getLogger(AbsAIChat.class);
    abstract protected ChatForUIIFC getChatForUIInstance();

    @Context
    private ServletContext servletContext;

    protected String getAbsoluteResourcePath() {
        String absoluteResourcePath = servletContext.getRealPath("/");
        return absoluteResourcePath;
    }

    protected String getRelavantResourcePath() {
        String relavantResourcePath = "/";
        return relavantResourcePath;
    }

    public WSModel.AIChatResponse send(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            String renderedResponse = getChatForUIInstance().fetchResponse(session, userInput, attachFiles);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

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
