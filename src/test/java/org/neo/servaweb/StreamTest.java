package org.neo.servaframe;

import java.util.*;
import java.sql.SQLException;

import okhttp3.*;
import java.io.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit test (JUnit Jupiter 5.13.4)
 */
public class StreamTest {

    @BeforeEach
    void setUp() throws Exception {
        // Code to set up resources or initialize variables before each test method
    }

    @AfterEach
    void tearDown() throws Exception {
        // Code to clean up resources after each test method
    }

    @Test
    void test1() {
        // no-op test preserved
    }

    @Test
    @Disabled("Was underscored in JUnit3 style; keep disabled unless explicitly enabled")
    void _testStream() throws Exception {
        OkHttpClient client = new OkHttpClient();
        String API_KEY = "<openAiApiKey>";
        // Construct the JSON payload with the prompt and enable streaming
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

                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        throw new IOException("Response body was null");
                    }
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

