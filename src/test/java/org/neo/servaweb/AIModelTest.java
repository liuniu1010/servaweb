package org.neo.servaframe;

import java.util.*;
import java.io.*;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.neo.servaframe.util.*;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaweb.*;
import org.neo.servaweb.ifc.*;
import org.neo.servaweb.impl.*;
import org.neo.servaweb.model.*;
import org.neo.servaweb.util.*;

/**
 * Unit test 
 */
public class AIModelTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AIModelTest( String testName ) {
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
        return new TestSuite( AIModelTest.class );
    }

    public void testTransform() throws Exception {
        InputStream in = new FileInputStream("/tmp/dogandcat.png");
        String base64 = IOUtil.inputStreamToBase64(in);

        AIModel.AttachmentAsBase64 attachment1 = new AIModel.PngFileAsBase64();
        attachment1.setBase64(base64);

        List<AIModel.Attachment> attachments1 = new ArrayList<AIModel.Attachment>();
        attachments1.add(attachment1);

        AIModel.AttachmentGroup attachmentGroup1 = new AIModel.AttachmentGroup();
        attachmentGroup1.setAttachments(attachments1);

        JsonObject jsonGroup1 = attachmentGroup1.toJsonObject();
        String jsonGroup1InString = new Gson().toJson(jsonGroup1);

        JsonElement element = JsonParser.parseString(jsonGroup1InString);
        JsonObject jsonMiddle = element.getAsJsonObject();

        AIModel.AttachmentGroup attachmentGroup2 = AIModel.AttachmentGroup.fromJsonObject(jsonMiddle);
        JsonObject jsonGroup2 = attachmentGroup2.toJsonObject();
        String jsonGroup2InString = new Gson().toJson(jsonGroup2);

        assertEquals(jsonGroup1InString, jsonGroup2InString);
    }
}
 
