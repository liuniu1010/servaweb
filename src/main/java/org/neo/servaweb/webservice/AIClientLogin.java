package org.neo.servaweb.webservice;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;

import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.NeoAIException;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AccessAgentIFC;
import org.neo.servaaiagent.impl.AccountAgentImpl;
import org.neo.servaaiagent.impl.AccessAgentImpl;

@Path("/aiclientlogin")
public class AIClientLogin {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AIClientLogin.class);

    @POST
    @Path("/sendpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse sendPassword(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        String username = params.getLoginSession();
        String sourceIP = getSourceIP(request);

        logger.info("User: " + username + " from " + sourceIP + " try to sendpassword");

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            checkAccessibilityOnAction(username, sourceIP);
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
        String username = params.getLoginSession();
        String password = params.getUserInput();
        String sourceIP = getSourceIP(request);

        logger.info("User: " + username + " from " + sourceIP + " try to login");

        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        try {
            checkAccessibilityOnAction(username, sourceIP);
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
        String loginSession = params.getLoginSession();

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

    @POST
    @Path("/getgoogleoauthurl")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse getGoogleOAuthUrl(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        String sourceIP = getSourceIP(request);
        logger.info("IP: " + sourceIP + " try to getGoogleOAuthUrl");
        String originalPage = params.getUserInput();

        try {
            checkAccessibilityOnOAuthLogin(sourceIP);
            String googleOAuthUrl = innerGetGoogleOAuthUrl(originalPage);
            WSModel.AIChatResponse chatResponse = new WSModel.AIChatResponse(true, googleOAuthUrl);
            return chatResponse;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    @GET
    @Path("/oauthlogin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response OAuthLogin(@Context HttpServletRequest request, @Context HttpServletResponse response, @QueryParam("code") String code, @QueryParam("state") String state) {
        String sourceIP = getSourceIP(request);
        logger.info("IP: " + sourceIP + " try to login with OAuth");

        try {
            checkAccessibilityOnOAuthLogin(sourceIP);

            if (code == null || code.isEmpty()) {
                throw new NeoAIException("Missing 'code' parameter");
            }

            String tokenJson = exchangeCodeForToken(code);
            JsonObject tokenObj = JsonParser.parseString(tokenJson).getAsJsonObject();
            String accessToken = tokenObj.get("access_token").getAsString();
            String userEmail = fetchUserEmail(accessToken);
            logger.info("userEmail = " + userEmail);

            AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
            String loginSession = accountAgent.loginWithOAuth(userEmail, sourceIP);

            String originalPage = state;
            String redirectUri = state + "?loginSession=" + loginSession;
            return Response.status(Response.Status.FOUND)
                   .location(URI.create(redirectUri))
                   .build();
        } 
        catch(Exception ex) {
            logger.error(ex.getMessage());
            standardHandleException(ex, response);
        }
        return null;
    }

    private String exchangeCodeForToken(String code) throws Exception {
        String TOKEN_ENDPOINT = CommonUtil.getConfigValue("OAuthGoogleTokenEndpoint");
        String CLIENT_ID = CommonUtil.getConfigValue("OAuthGoogleClientID");
        String CLIENT_SECRET = CommonUtil.getConfigValue("OAuthGoogleClientSecret");
        String REDIRECT_URI = CommonUtil.getConfigValue("OAuthGoogleRedirectUri");

        URL url = new URL(TOKEN_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code=" + URLEncoder.encode(code, "UTF-8")
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                + "&grant_type=authorization_code";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = conn.getInputStream()) {
            return new String(readAllBytesCompat(is), StandardCharsets.UTF_8);
        }
    }

    private String fetchUserEmail(String accessToken) throws Exception {
        String USERINFO_ENDPOINT = CommonUtil.getConfigValue("OAuthGoogleUserInfoEndpoint");
        URL url = new URL(USERINFO_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        try (InputStream is = conn.getInputStream()) {
            String json = new String(readAllBytesCompat(is), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.get("email").getAsString();
        }
    }

    private byte[] readAllBytesCompat(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    private String innerGetGoogleOAuthUrl(String originalPage) throws Exception {
        String[] configNames = new String[]{"OAuthGoogleClientID"
                                           ,"OAuthGoogleRedirectUri"};
        Map<String, String> configMap = CommonUtil.getConfigValues(configNames);

        String OAuthGoogleClientID = configMap.get("OAuthGoogleClientID");
        String OAuthGoogleRedirectUri = configMap.get("OAuthGoogleRedirectUri");
        String scope = "https://www.googleapis.com/auth/userinfo.email";
        String responseType = "code";
        String state = originalPage;
        String accessType = "offline";
        String prompt = "select_account";

        String oauthUrl = "https://accounts.google.com/o/oauth2/v2/auth";
        oauthUrl += "?client_id=" + URLEncoder.encode(OAuthGoogleClientID, StandardCharsets.UTF_8.toString());
        oauthUrl += "&redirect_uri=" + URLEncoder.encode(OAuthGoogleRedirectUri, StandardCharsets.UTF_8.toString());
        oauthUrl += "&response_type=" + responseType;
        oauthUrl += "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8.toString());
        oauthUrl += "&access_type=" + accessType;
        oauthUrl += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8.toString());
        oauthUrl += "&prompt=" + prompt;

        return oauthUrl;
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

    private void checkAccessibilityOnAction(String username, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnAction(dbConnection, username, sourceIP);
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

    private void checkAccessibilityOnOAuthLogin(String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                try {
                    innerCheckAccessibilityOnOAuthLogin(dbConnection, sourceIP);
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

    private void innerCheckAccessibilityOnAction(DBConnectionIFC dbConnection, String username, String sourceIP) {
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        if(accessAgent.verifyAdminByUsername(dbConnection, username)) {
            return;
        }
        if(accessAgent.verifyMaintenance(dbConnection)) {
            return;
        }
        if(accessAgent.verifyUsername(dbConnection, username)) {
            return;
        }
        if(accessAgent.verifyIP(dbConnection, sourceIP)) {
            return;
        }
        if(accessAgent.verifyRegion(dbConnection, sourceIP)) {
            return;
        }

        // by default, pass
    }

    private void innerCheckAccessibilityOnOAuthLogin(DBConnectionIFC dbConnection, String sourceIP) {
        AccessAgentIFC accessAgent = AccessAgentImpl.getInstance();
        if(accessAgent.verifyMaintenance(dbConnection)) {
            return;
        }
        if(accessAgent.verifyIP(dbConnection, sourceIP)) {
            return;
        }
        if(accessAgent.verifyRegion(dbConnection, sourceIP)) {
            return;
        }

        // by default, pass
    }
}
