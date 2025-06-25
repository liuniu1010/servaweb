package org.neo.servaweb.webservice;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.NeoAIException;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;
import org.neo.servaaiagent.model.AgentModel;

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

    protected String alignSession(String loginSession) {
        return getHook() + loginSession;
    }

    public WSModel.AIChatResponse send(WSModel.AIChatParams params) {
        try {
            String loginSession = params.getLoginSession();
            String alignedSession = alignSession(loginSession);
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            AgentModel.UIParams uiParams = new AgentModel.UIParams();
            uiParams.setAlignedSession(alignedSession);
            uiParams.setLoginSession(loginSession);
            uiParams.setUserInput(userInput);
            uiParams.setAttachFiles(attachFiles);
            String renderedResponse = getChatForUIInstance().fetchResponse(uiParams);

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
            String loginSession = params.getLoginSession();
            String alignedSession = alignSession(loginSession);
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            AgentModel.UIParams uiParams = new AgentModel.UIParams();
            uiParams.setAlignedSession(alignedSession);
            uiParams.setLoginSession(loginSession);
            uiParams.setUserInput(userInput);
            uiParams.setAttachFiles(attachFiles);
            String renderedResponse = getChatForUIInstance().sendAudio(uiParams);

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
            String loginSession = params.getLoginSession();
            String alignedSession = alignSession(loginSession);
            String userInput = params.getUserInput();
            String fileAsBase64 = params.getFileAsBase64();

            List<String> attachFiles = null;
            if(fileAsBase64 != null 
                && !fileAsBase64.trim().equals("")) {
                attachFiles = new ArrayList<String>();
                attachFiles.add(fileAsBase64.trim());
            }
            AgentModel.UIParams uiParams = new AgentModel.UIParams();
            uiParams.setAlignedSession(alignedSession);
            uiParams.setLoginSession(loginSession);
            uiParams.setUserInput(userInput);
            uiParams.setNotifyCallback(notifyCallback);
            uiParams.setAttachFiles(attachFiles);
            String renderedResponse = getChatForUIInstance().fetchResponse(uiParams);

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
            String loginSession = params.getLoginSession();
            String alignedSession = alignSession(loginSession);
            String userInput = params.getUserInput();

            AgentModel.UIParams uiParams = new AgentModel.UIParams();
            uiParams.setAlignedSession(alignedSession);
            uiParams.setLoginSession(loginSession);
            uiParams.setUserInput(userInput);
            String renderedResponse = getChatForUIInstance().echo(uiParams);

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
            String loginSession = params.getLoginSession();
            String alignedSession = alignSession(loginSession);

            AgentModel.UIParams uiParams = new AgentModel.UIParams();
            uiParams.setAlignedSession(alignedSession);
            uiParams.setLoginSession(loginSession);
            uiParams.setSayHello(sayHello);
            String renderedResponse = getChatForUIInstance().initNewChat(uiParams);

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
            String loginSession = params.getLoginSession();
            String alignedSession = alignSession(loginSession);

            AgentModel.UIParams uiParams = new AgentModel.UIParams();
            uiParams.setAlignedSession(alignedSession);
            uiParams.setLoginSession(loginSession);
            String renderedResponse = getChatForUIInstance().refresh(uiParams);

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

    protected void checkAccessibilityOnClientAction(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnClientAction(dbConnection, loginSession);
                }
                catch(NeoAIException nex) {
                    throw nex;
                }
                catch(Exception ex) {
                    throw new NeoAIException(ex.getMessage(), ex);
                }
                return null;
            }
        });
    }

    private void updateSessionAndEnsureCredits(DBConnectionIFC dbConnection, String loginSession) {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.checkSessionValid(dbConnection, loginSession);
        accountAgent.updateSession(dbConnection, loginSession);
        accountAgent.checkCreditsWithSession(dbConnection, loginSession);
    }

    private void innerCheckAccessibilityOnClientAction(DBConnectionIFC dbConnection, String loginSession) {
        updateSessionAndEnsureCredits(dbConnection, loginSession);
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        if(accessAgent.verifyAdminByLoginSession(dbConnection, loginSession)) {
            return;
        }
        if(accessAgent.verifyMaintenance(dbConnection)) {
            return;
        }

        // by default, pass
    }

    protected void checkAccessibilityOnAdminAction(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnAdminAction(dbConnection, loginSession);
                }
                catch(NeoAIException nex) {
                    throw nex;
                }
                catch(Exception ex) {
                    throw new NeoAIException(ex.getMessage(), ex);
                }
                return null;
            }
        });
    }

    private void innerCheckAccessibilityOnAdminAction(DBConnectionIFC dbConnection, String loginSession) {
        updateSessionAndEnsureCredits(dbConnection, loginSession);
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        accessAgent.ensureAdminByLoginSession(dbConnection, loginSession);
    }

    protected void standardHandleException(Exception ex, HttpServletResponse response) {
        terminateConnection(decideHttpResponseStatus(ex), ex.getMessage(), response);
    }

    private int decideHttpResponseStatus(Exception ex) {
        if(ex instanceof NeoAIException) {
            NeoAIException nex = (NeoAIException)ex;
            if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_SESSION_INVALID
                || nex.getCode() == NeoAIException.NEOAIEXCEPTION_LOGIN_FAIL
                || nex.getCode() == NeoAIException.NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST) {
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
            else if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_NOCREDITS_LEFT) {
                return HttpServletResponse.SC_PAYMENT_REQUIRED;
            }
        }
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private void terminateConnection(int httpStatus, String message, HttpServletResponse response) {
        try {
            response.setStatus(httpStatus);
            response.getWriter().write(message);
            response.flushBuffer();
            return;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
