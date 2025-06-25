package org.neo.servaweb.webservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;

@Path("/aiclientcredit")
public class AIClientCredit {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AIClientCredit.class);

    @POST
    @Path("/getcreditcount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse getCreditCount(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        String sourceIP = getSourceIP(request);

        logger.info("loginSession: " + loginSession + " from " + sourceIP + " try to get credit count");

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            checkAccessibilityOnAction(loginSession, sourceIP);
            int credits = accountAgent.getLeftCreditsWithSession(loginSession);
            WSModel.AIChatResponse chatResponse = new WSModel.AIChatResponse(true, String.valueOf(credits));
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null; 
    }

    @POST
    @Path("/getpaymentlink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse getPaymentLink(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        String sourceIP = getSourceIP(request);

        logger.info("loginSession: " + loginSession + " from " + sourceIP + " try to get payment link");

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            checkAccessibilityOnAction(loginSession, sourceIP);
            String userName = accountAgent.getUserNameWithSession(loginSession);
            String paymentLink = CommonUtil.getConfigValue("paymentLinkOnStripe");
            paymentLink = paymentLink + "?prefilled_email=" + userName;
            WSModel.AIChatResponse chatResponse = new WSModel.AIChatResponse(true, paymentLink);
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
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
                || nex.getCode() == NeoAIException.NEOAIEXCEPTION_LOGIN_FAIL
                || nex.getCode() == NeoAIException.NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST) {
                return HttpServletResponse.SC_UNAUTHORIZED;
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

    private void checkAccessibilityOnAction(String loginSession, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnAction(dbConnection, loginSession, sourceIP);
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

    private void innerCheckAccessibilityOnAction(DBConnectionIFC dbConnection, String loginSession, String sourceIP) {
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.checkSessionValid(dbConnection, loginSession);
        if(accessAgent.verifyAdminByLoginSession(dbConnection, loginSession)) {
            return;
        }
        if(accessAgent.verifyMaintenance(dbConnection)) {
            return;
        }

        // by default, pass
    }
}
