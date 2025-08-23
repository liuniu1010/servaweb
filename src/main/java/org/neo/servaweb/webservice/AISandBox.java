package org.neo.servaweb.webservice;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.neo.servaframe.util.IOUtil;
import org.neo.servaaiagent.ifc.ShellAgentIFC;
import org.neo.servaaiagent.impl.ShellAgentInMemoryImpl;

@Path("/aisandbox")
public class AISandBox {
    @POST
    @Path("/executecommand")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse executeCommand(WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        String command = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            String result = shellAgent.execute(loginSession, command);
            chatResponse = new WSModel.AIChatResponse(true, result);
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }

    @POST
    @Path("/terminateshell")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse terminateShell(WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            shellAgent.terminateShell(loginSession);
            chatResponse = new WSModel.AIChatResponse(true, "shell closed success");
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }

    @POST
    @Path("/isunix")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse isUnix(WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            boolean isUnix = shellAgent.isUnix(loginSession);
            chatResponse = new WSModel.AIChatResponse(true, isUnix?"yes":"no");
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }

    @POST
    @Path("/downloadproject")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse downloadProject(WSModel.AIChatParams params) {
        String loginSession = params.getLoginSession();
        String projectPath = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            String tarFilePath = "/tmp/" + loginSession + ".tar.gz"; 
            String command = "cd " + projectPath + "/../ && tar -zcvf " + tarFilePath + " myProject/";
            shellAgent.execute(loginSession, command);

            String base64 = IOUtil.fileToRawBase64(tarFilePath);
            chatResponse = new WSModel.AIChatResponse(true, base64);
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }
}
