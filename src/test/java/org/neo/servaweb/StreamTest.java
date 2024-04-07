package org.neo.servaframe;

import java.util.*;
import java.sql.SQLException;

import okhttp3.*;
import java.io.*;

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
import org.neo.servaweb.util.*;

/**
 * Unit test 
 */
public class StreamTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public StreamTest( String testName ) {
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
        return new TestSuite( StreamTest.class );
    }

    public void testStream() throws Exception {
        OkHttpClient client = new OkHttpClient();
        String API_KEY = "<openAiApiKey>";
        // Constructing the JSON payload with the prompt and enabling streaming
        String jsonPayload = "{" +
            "\"model\": \"gpt-4\"," +
            "\"messages\": [{" +
            "   \"role\": \"user\"," +
            "   \"content\": \"Please give me a plan to learn python in 3 months\"" +
            "}]," +
            "\"stream\": true" +
            "}";

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(jsonPayload, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Assuming the response body is not null
                try (ResponseBody responseBody = response.body()) {
                    // Handle the stream. For example, read line by line:
                    assert responseBody != null;
                    BufferedReader reader = new BufferedReader(responseBody.charStream());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }
        });

        System.out.println("in sleep");
        Thread.sleep(10000);
    }
}

