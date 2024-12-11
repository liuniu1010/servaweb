package org.neo.servaweb.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
        String session = params.getSession();
        String command = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            String result = shellAgent.execute(session, command);
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
        String session = params.getSession();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            shellAgent.terminateShell(session);
            chatResponse = new WSModel.AIChatResponse(true, "shell closed success");
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
        String session = params.getSession();
        String projectPath = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            ShellAgentIFC shellAgent = ShellAgentInMemoryImpl.getInstance();
            String tarFilePath = "/tmp/" + session + ".tar.gz"; 
            String command = "cd " + projectPath + "/../ && tar -zcvf " + tarFilePath + " myProject/";
            shellAgent.execute(session, command);

            String base64 = IOUtil.fileToRawBase64(tarFilePath);
            chatResponse = new WSModel.AIChatResponse(true, base64);
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }
}
