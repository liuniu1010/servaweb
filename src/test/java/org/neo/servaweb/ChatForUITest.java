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
public class ChatForUITest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ChatForUITest( String testName ) {
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
        return new TestSuite( ChatForUITest.class );
    }

    private String[] getSupportModels() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new GetModelTask());
    }

    public void testChat() throws Exception {
        ChatForUIIFC chatForUIIFC = ChatForUIImpl.getInstance();
        String session = "role1ToRole2En";
        // String userInput = "sorry, I forgot my own name, can you tell me?";
        String result = chatForUIIFC.refresh(session);
        String filePath = "/tmp/botwithbotEn.html";
        IOUtil.stringToFile(result, filePath);
    }
}

