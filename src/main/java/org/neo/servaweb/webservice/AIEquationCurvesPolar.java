package org.neo.servaweb.webservice;

import jakarta.servlet.AsyncContext;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import java.nio.charset.StandardCharsets;

@Path("/aiequationcurvespolar")
public class AIEquationCurvesPolar extends AIUtilityBot {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AIEquationCurvesPolar.class);
    final static String HOOK = "aiequationcurvespolar";

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
