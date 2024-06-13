package org.neo.servaweb.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.neo.servaaibase.util.CommonUtil;

@Path("/aisandbox")
public class AISandBox {
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse executeCommand(WSModel.AIChatParams params) {
        String command = params.getUserInput();
        WSModel.AIChatResponse chatResponse = null;

        try {
            String result = CommonUtil.executeCommand(command);
            chatResponse = new WSModel.AIChatResponse(true, command);
        }
        catch(Exception ex) {
            chatResponse = new WSModel.AIChatResponse(false, ex.getMessage());
        }
        return chatResponse;
    }
}
