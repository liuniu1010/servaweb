package org.neo.servaframe;

import java.util.*;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.neo.servaframe.util.ConfigUtil;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaweb.*;
import org.neo.servaweb.ifc.*;
import org.neo.servaweb.impl.*;
import org.neo.servaweb.model.*;

/**
 * Unit test 
 */
public class OpenAIImplTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OpenAIImplTest( String testName ) {
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
        return new TestSuite( OpenAIImplTest.class );
    }

    private String[] getSupportModels() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new GetModelTask());
    }

    private String fetchChatResponse(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeQueryTask(new FetchChatResponseTask(userInput));
    }

    public void testGetSupportModels() {
        String[] models = getSupportModels();
        System.out.println("models.size = " + models.length);
        for(String model: models) {
            System.out.println("model = " + model);
        }
    }

    public void testFetchChatResponse() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            String response = fetchChatResponse(userInput);
            System.out.println("userInput = " + userInput);
            System.out.println("response = " + response);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    
}

class GetModelTask implements DBQueryTaskIFC {
    @Override
    public Object query(DBConnectionIFC dbConnection) {
        OpenAIImpl superAI = new OpenAIImpl();
        superAI.setDBConnection(dbConnection);
        String[] models = superAI.getSupportModels();
        return models;
    }
}

class FetchChatResponseTask implements DBQueryTaskIFC {
    private String userInput;

    public FetchChatResponseTask(String inputUserInput) {
        userInput = inputUserInput;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        OpenAIImpl superAI = new OpenAIImpl();
        superAI.setDBConnection(dbConnection);
        String[] models = superAI.getSupportModels();
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        promptStruct.setUserInput(userInput);
        AIModel.ChatResponse chatResponse = superAI.fetchChatResponse(models[0], promptStruct);
        return chatResponse.getMessage(); 
    }
}