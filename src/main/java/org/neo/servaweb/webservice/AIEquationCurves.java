package org.neo.servaweb.webservice;

import javax.servlet.AsyncContext;
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

@Path("/aiequationcurves")
public class AIEquationCurves extends AIUtilityBot {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AIEquationCurves.class);
    final static String HOOK = "aiequationcurves";

    @Override
    protected String getHook() {
        return HOOK;
    }

    @POST
    @Path("/streamsend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamsend(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        super.streamsend(request, response, params);
    }

    @POST
    @Path("/streamrefresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamrefresh(@Context HttpServletRequest request, @Context HttpServletResponse response, WSModel.AIChatParams params) {
        super.streamrefresh(request, response, params);
    }

    @POST
    @Path("/newchat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WSModel.AIChatResponse newchat(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        return super.newchat(response, params);
    }

    @POST
    @Path("/undo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void undo(@Context HttpServletResponse response, WSModel.AIChatParams params) {
        super.undo(response, params);
    }
}
