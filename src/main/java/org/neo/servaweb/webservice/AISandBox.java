package org.neo.servaweb.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.neo.servaframe.util.IOUtil;
import org.neo.servaaibase.util.CommonUtil;

@Path("/aisandbox")
public class AISandBox {
    @POST
    @Path("/executecommand")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse executecommand(WSModel.AIChatParams params) {
        String command = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            String result = CommonUtil.executeCommand(command);
            chatResponse = new WSModel.AIChatResponse(true, result);
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }

    @POST
    @Path("/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse download(WSModel.AIChatParams params) {
        String session = params.getSession();
        String projectPath = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            String tarFilePath = "/tmp/" + session + ".tar.gz"; 
            String command = "cd " + projectPath + "/../ && tar -zcvf " + tarFilePath + " myProject/";
            CommonUtil.executeCommand(command);

            String base64 = IOUtil.fileToRawBase64(tarFilePath);
            chatResponse = new WSModel.AIChatResponse(true, base64);
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }
}
