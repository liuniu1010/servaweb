package org.neo.servaweb.ifc;

/*
 * this interface will return response directly
 * without any rendering
 */
public interface ChatIFC {
    public String fetchResponse(String session, String userInput);
    public void initNewChat(String session);
}
