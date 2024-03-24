package org.neo.servaweb.impl;

import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.neo.servaframe.util.IOUtil;
import org.neo.servaweb.ifc.SuperAIIFC;
import org.neo.servaweb.model.AIModel;

abstract public class AbsOpenAIImpl implements SuperAIIFC {
    final static Logger logger = Logger.getLogger(AbsOpenAIImpl.class);

    abstract protected String getApiKey();
    abstract protected String getUrl(String model);
    abstract protected int getMaxOutputTokenNumber(String model);
    abstract protected int getContextWindow(String model);
    abstract protected String getSystemHint();

    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct) {
        try {
            AIModel.ChatResponse chatResponse = fetchJsonResponseWithChatCompletion(model, promptStruct);
            return chatResponse;
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private AIModel.ChatResponse fetchJsonResponseWithChatCompletion(String model, AIModel.PromptStruct promptStruct) throws Exception {
        int maxTokens = determineMaxTokens(model, promptStruct);
        AIModel.ChatResponse chatResponse = fetchChatResponse(model, promptStruct, maxTokens);
        return chatResponse;
    }

    private int determineMaxTokens(String model, AIModel.PromptStruct promptStruct) throws Exception {
        int promptTokenNumber = fetchPromptTokenNumber(model, promptStruct);
        if(promptTokenNumber < 0) {
            throw new RuntimeException("some error occurred for promptTokenNumber < 0");
        }

        return Math.min(getMaxOutputTokenNumber(model), (getContextWindow(model) - promptTokenNumber));
    }

    private AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) throws Exception {
        String jsonInput = generateJsonBodyToFetchResponse(model, promptStruct, maxTokens);
        String jsonResponse = send(model, jsonInput);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        return chatResponse;
    }

    private int fetchPromptTokenNumber(String model, AIModel.PromptStruct promptStruct) throws Exception {
        String jsonInput = generateJsonBodyForGetTokenNumber(model, promptStruct);
        String jsonTokenNumber = send(model, jsonInput);
        int tokenNumber = extractTokenNumberFromJson(jsonTokenNumber);
        return tokenNumber;
    }

    private int extractTokenNumberFromJson(String jsonTokenNumber) {
        JsonElement element = JsonParser.parseString(jsonTokenNumber);
        JsonObject jsonObject = element.getAsJsonObject();
        int tokenNumber = jsonObject.getAsJsonObject("usage").get("prompt_tokens").getAsInt();
        return tokenNumber;
    }

    private AIModel.ChatResponse extractChatResponseFromJson(String jsonResponse) throws Exception {
        AIModel.ChatResponse chatResponse = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("choices")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject messageObject = firstChoice.getAsJsonObject("message");
            String content = messageObject.get("content").getAsString();

            chatResponse = new AIModel.ChatResponse(true, content);
        }
        else {
            String errorMessage = jsonObject.getAsJsonObject("error").get("message").getAsString();

            chatResponse = new AIModel.ChatResponse(false, errorMessage);
        }
        return chatResponse; 
    }

    private JsonArray generateJsonArrayMessages(AIModel.PromptStruct promptStruct) {
        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", getSystemHint());
        messages.add(systemMessage);

        List<AIModel.ChatRecord> chatRecords = promptStruct.getChatRecords();
        for(AIModel.ChatRecord chatRecord: chatRecords) {
            JsonObject recordMessage = new JsonObject();
            recordMessage.addProperty("role", chatRecord.getIsRequest()?"user":"assistant");
            recordMessage.addProperty("content", chatRecord.getContent());
            messages.add(recordMessage);
        }

        JsonObject userInputMessage = new JsonObject();
        userInputMessage.addProperty("role", "user");
        userInputMessage.addProperty("content", promptStruct.getUserInput());
        messages.add(userInputMessage);

        return messages;
    }

    private String generateJsonBodyForGetTokenNumber(String model, AIModel.PromptStruct promptStruct) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", 1);
        jsonBody.addProperty("temperature", 0);
        jsonBody.addProperty("n", 1);
        jsonBody.addProperty("stop", "");

        JsonArray messages = generateJsonArrayMessages(promptStruct);

        jsonBody.add("messages", messages);
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", maxTokens);
        jsonBody.addProperty("temperature", 0.5);

        JsonArray messages = generateJsonArrayMessages(promptStruct);

        jsonBody.add("messages", messages);
        return gson.toJson(jsonBody);
    }

    private String send(String model, String jsonInput) throws Exception {
        logger.debug("call openai api, model = " + model + ", jsonInput = " + jsonInput);
        URL url = new URL(getUrl(model));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + getApiKey());

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()){
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            try (InputStream in = connection.getInputStream()){
                String response = IOUtil.inputStreamToString(in);
                logger.debug("return from openai api, response = " + response);
                return response;
            }
        }
        finally {
            connection.disconnect();
        }
    }
}
