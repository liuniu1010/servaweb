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

import org.neo.servaaibase.NeoAIException;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

abstract public class AbsAIChat {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbsAIChat.class);
    abstract protected ChatForUIIFC getChatForUIInstance();
    abstract protected String getHook();

    @Context
    private ServletContext servletContext;

    protected String getAbsoluteResourcePath() {
        String absoluteResourcePath = servletContext.getRealPath("/");
        return absoluteResourcePath;
    }

    protected String getRelevantResourcePath() {
        String relevantResourcePath = "/";
        return relevantResourcePath;
    }

    protected String alignSession(String session) {
        return getHook() + session;
    }

    public WSModel.AIChatResponse send(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String alignedSession = alignSession(session);
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            String renderedResponse = getChatForUIInstance().fetchResponse(alignedSession, userInput, attachFiles);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    public WSModel.AIChatResponse sendAudio(WSModel.AIChatParams params) {
        try {
            String session = params.getSession();
            String alignedSession = alignSession(session);
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            String renderedResponse = getChatForUIInstance().sendAudio(alignedSession, userInput, attachFiles);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    public WSModel.AIChatResponse streamsend(WSModel.AIChatParams params, NotifyCallbackIFC notifyCallback) {
        try {
            String session = params.getSession();
            String alignedSession = alignSession(session);
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            String renderedResponse = getChatForUIInstance().fetchResponse(alignedSession, notifyCallback, userInput, attachFiles);

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
            String alignedSession = alignSession(session);
            String userInput = params.getUserInput();
            String renderedResponse = getChatForUIInstance().echo(alignedSession, userInput);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    public WSModel.AIChatResponse newchat(WSModel.AIChatParams params) {
        String defaultSayHello = "Hello, how can I help you?";
        return newchat(params, defaultSayHello);
    }

    public WSModel.AIChatResponse newchat(WSModel.AIChatParams params, String sayHello) {
        try {
            String session = params.getSession();
            String alignedSession = alignSession(session);
            String renderedResponse = getChatForUIInstance().initNewChat(alignedSession, sayHello);

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
            String alignedSession = alignSession(session);
            String renderedResponse = getChatForUIInstance().refresh(alignedSession);

            WSModel.AIChatResponse response = new WSModel.AIChatResponse(true, renderedResponse);
            return response;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new WSModel.AIChatResponse(false, ex.getMessage());
        }
    }

    protected boolean isBase64SizeValid(String base64String) {
        if (base64String == null) {
            return true;
        }

        // Remove Base64 metadata if present (e.g., "data:audio/mp3;base64,")
        if (base64String.contains(",")) {
            base64String = base64String.split(",")[1];
        }

        // Calculate approximate decoded size
        long decodedSize = (long) (base64String.length() * (3.0 / 4.0));

        // Validate the file size
        long maxSize = CommonUtil.getConfigValueAsInt("maxFileSizeForUpload") * 1024 * 1024;
        return decodedSize <= maxSize;
    }
}
