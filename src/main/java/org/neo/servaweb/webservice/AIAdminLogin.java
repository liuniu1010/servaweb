package org.neo.servaweb.webservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.nio.charset.StandardCharsets;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;

@Path("/aiadminlogin")
public class AIAdminLogin {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AIAdminLogin.class);

    @POST
    @Path("/sendpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse sendPassword(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        String username = params.getSession();
        String sourceIP = getSourceIP(request);

        logger.info("User: " + username + " from " + sourceIP + " try to sendpassword");

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            checkAccessibilityOnSendPassword(username, sourceIP);
            accountAgent.sendPassword(username, sourceIP);
            WSModel.AIChatResponse chatResponse = new WSModel.AIChatResponse(true, "The new password has been sent to your email address");
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null; 
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse login(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        String username = params.getSession();
        String password = params.getUserInput();
        String sourceIP = getSourceIP(request);

        logger.info("User: " + username + " from " + sourceIP + " try to login");

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            checkAccessibilityOnLogin(username, sourceIP);
            String loginSession = accountAgent.login(username, password, sourceIP);
            WSModel.AIChatResponse chatResponse = new WSModel.AIChatResponse(true, loginSession);
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null; 
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse logout(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        String loginSession = params.getSession();

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            accountAgent.logout(loginSession);
            WSModel.AIChatResponse chatResponse = new WSModel.AIChatResponse(true, "");
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null; 
    }

    private String getSourceIP(HttpServletRequest request) {
        String sourceIP = request.getHeader("X-Forwarded-For");
        if (sourceIP == null || sourceIP.isEmpty()) {
            sourceIP = request.getRemoteAddr(); // fallback
        } 
        else {
            // In case there are multiple IPs (comma-separated), take the first one
            sourceIP = sourceIP.split(",")[0].trim();
        }   
        return sourceIP;
    }

    private void standardHandleException(Exception ex, HttpServletResponse response) {
        terminateConnection(decideHttpResponseStatus(ex), ex.getMessage(), response);
    }

    private int decideHttpResponseStatus(Exception ex) {
        if(ex instanceof NeoAIException) {
            NeoAIException nex = (NeoAIException)ex;
            if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_SESSION_INVALID
                || nex.getCode() == NeoAIException.NEOAIEXCEPTION_LOGIN_FAIL) {
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

    private void checkAccessibilityOnSendPassword(String username, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnSendPassword(dbConnection, username, sourceIP);
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

    private void innerCheckAccessibilityOnSendPassword(DBConnectionIFC dbConnection, String username, String sourceIP) {
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        if(accessAgent.verifyAdminByUsername(dbConnection, username)) {
            return;
        }
    }

    private void checkAccessibilityOnLogin(String username, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnLogin(dbConnection, username, sourceIP);
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

    private void innerCheckAccessibilityOnLogin(DBConnectionIFC dbConnection, String username, String sourceIP) {
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        if(accessAgent.verifyAdminByUsername(dbConnection, username)) {
            return;
        }
    }
}
