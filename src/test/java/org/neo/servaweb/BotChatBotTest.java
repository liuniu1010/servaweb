package org.neo.servaframe;

import java.util.*;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.neo.servaframe.util.*;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaweb.*;
import org.neo.servaweb.ifc.*;
import org.neo.servaweb.impl.*;
import org.neo.servaweb.model.*;

/**
 * Unit test 
 */
public class BotChatBotTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BotChatBotTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Code to set up resources or initialize variables before each test method
    }

    @Override
    protected void tearDown() throws Exception {
        // Code to clean up resources after each test method
        super.tearDown();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( BotChatBotTest.class );
    }

    private String[] getSupportModels() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new GetModelTask());
    }

    public void testChat() throws Exception {
        ChatIFC role1 = Role1ChatImpl.getInstance();
        ChatIFC role2 = Role2ChatImpl.getInstance();

        String input = "Hello！";

        int index = 20;
        while(index > 0) {
            input = role1ToRole2(input, role2);
            input = role2ToRole1(input, role1);
            index--;
            System.out.println("index = " + index);
        }
    }

    private String role1ToRole2(String input, ChatIFC role2) {
        String session = "role1ToRole2En";
        String response = role2.fetchResponse(session, input);
        return response;
    }

    private String role2ToRole1(String input, ChatIFC role1) {
        String session = "role2ToRole1En";
        String response = role1.fetchResponse(session, input); 
        return response;
    }
}

