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
import org.neo.servaweb.ifc.OpenAIIFC;
import org.neo.servaweb.ifc.FunctionCallIFC;
import org.neo.servaweb.model.AIModel;

abstract public class AbsOpenAIImpl implements OpenAIIFC {
    final static Logger logger = Logger.getLogger(AbsOpenAIImpl.class);

    abstract protected String getApiKey();
    abstract protected String getUrl(String model);
    abstract protected int getMaxOutputTokenNumber(String model);
    abstract protected int getContextWindow(String model);
    abstract protected String getSystemHint();

    @Override
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct) {
        try {
            AIModel.ChatResponse chatResponse = innerFetchChatResponse(model, promptStruct, null);
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

    @Override
    public AIModel.ChatResponse fetchChatResponseWithFunctionCall(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) {
        try {
            AIModel.ChatResponse chatResponse = innerFetchChatResponse(model, promptStruct, functionCallIFC);
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

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        int maxTokens = determineMaxTokens(model, promptStruct, functionCallIFC);
        AIModel.ChatResponse chatResponse = innerFetchChatResponse(model, promptStruct, maxTokens, functionCallIFC);
        return chatResponse;
    }

    private int determineMaxTokens(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        boolean needCalculate = false; // calculate prompt token number or not
        if(needCalculate) { // in this way, calculate prompt token number first
            int promptTokenNumber = fetchPromptTokenNumber(model, promptStruct, functionCallIFC);
            if(promptTokenNumber < 0) {
                throw new RuntimeException("some error occurred for promptTokenNumber < 0");
            }

            return Math.min(getMaxOutputTokenNumber(model), (getContextWindow(model) - promptTokenNumber));
        }
        else {
            return getMaxOutputTokenNumber(model); // in this way, don't calcuate prompt token number
        }
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens, FunctionCallIFC functionCallIFC) throws Exception {
        String jsonInput = generateJsonBodyToFetchResponse(model, promptStruct, maxTokens, functionCallIFC);
        String jsonResponse = send(model, jsonInput);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        return chatResponse;
    }

    private int fetchPromptTokenNumber(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        String jsonInput = generateJsonBodyForGetTokenNumber(model, promptStruct, functionCallIFC);
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

    private JsonArray generateJsonArrayTools(FunctionCallIFC functionCallIFC) {
        JsonArray tools = new JsonArray();

        List<AIModel.Function> functions = functionCallIFC.getFunctions();
        for(AIModel.Function function: functions) {
            JsonObject jsonFunction = new JsonObject();
            jsonFunction.addProperty("name", function.getMethodName());
            jsonFunction.addProperty("description", function.getDescription());

            JsonObject jsonParameters = new JsonObject();
            jsonParameters.addProperty("type", "object");
            List<AIModel.FunctionParam> functionParams = function.getParams();
            JsonObject jsonProperties = new JsonObject();
            JsonArray jsonRequiredParams = new JsonArray();
            for(AIModel.FunctionParam functionParam: functionParams) {
                JsonObject jsonParam = new JsonObject();
                jsonParam.addProperty("type", "string");
                jsonParam.addProperty("description", functionParam.getDescription());

                jsonProperties.add(functionParam.getName(), jsonParam);
                jsonRequiredParams.add(functionParam.getName()); 
            }
            jsonParameters.add("properties", jsonProperties);
            jsonParameters.add("required", jsonRequiredParams);
            jsonFunction.add("parameters", jsonParameters);

            JsonObject jsonTool = new JsonObject();
            jsonTool.addProperty("type", "function");
            jsonTool.add("function", jsonFunction);

            tools.add(jsonTool);
        }
        return tools;
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

    private String generateJsonBodyForGetTokenNumber(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", 1);
        jsonBody.addProperty("temperature", 0);
        jsonBody.addProperty("n", 1);
        jsonBody.addProperty("stop", "");

        JsonArray messages = generateJsonArrayMessages(promptStruct);
        jsonBody.add("messages", messages);

        if(functionCallIFC != null) {
            JsonArray tools = generateJsonArrayTools(functionCallIFC);
            jsonBody.add("tools", tools);
        }
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens, FunctionCallIFC functionCallIFC) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", maxTokens);
        jsonBody.addProperty("temperature", 0.5);

        JsonArray messages = generateJsonArrayMessages(promptStruct);
        jsonBody.add("messages", messages);

        if(functionCallIFC != null) {
            JsonArray tools = generateJsonArrayTools(functionCallIFC);
            jsonBody.add("tools", tools);
        }
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