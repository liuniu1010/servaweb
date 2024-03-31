package org.neo.servaweb.impl;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
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
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) {
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

    @Override
    public AIModel.Embedding getEmbedding(String model, String input) {
        try {
            AIModel.Embedding embedding = innerGetEmbedding(model, input, -1);
            return embedding;
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
    public AIModel.Embedding getEmbedding(String model, String input, int dimensions) {
        try {
            AIModel.Embedding embedding = innerGetEmbedding(model, input, dimensions);
            return embedding;
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

    private AIModel.Embedding innerGetEmbedding(String model, String input, int dimensions) throws Exception {
        String jsonInput = generateJsonBodyToGetEmbedding(model, input, dimensions);
        String jsonResponse = send(model, jsonInput);
        AIModel.Embedding embedding = extractEmbeddingFromJson(jsonResponse);
        return embedding;
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
        List<AIModel.Call> calls = extractCallsFromJson(jsonResponse);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        chatResponse.setCalls(calls);
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

    private AIModel.Embedding extractEmbeddingFromJson(String jsonResponse) {
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();

        JsonArray dataArray = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject().getAsJsonArray("embedding");

        int size = dataArray.size();
        double[] data = new double[size];
        for(int i = 0;i < size;i++) {
            data[i] = dataArray.get(i).getAsDouble();
        }

        AIModel.Embedding embedding = new AIModel.Embedding(data);
        return embedding;
    }

    private List<AIModel.Call> extractCallsFromJson(String jsonResponse) throws Exception {
        List<AIModel.Call> calls = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("choices")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject messageObject = firstChoice.getAsJsonObject("message");

            if(!messageObject.has("tool_calls")) {
                return null;
            }
            JsonArray toolCallsArray = messageObject.getAsJsonArray("tool_calls");
            if(toolCallsArray != null
                && !toolCallsArray.isJsonNull()
                && toolCallsArray.size() > 0) {
                calls = new ArrayList<AIModel.Call>();
                for(int i = 0;i < toolCallsArray.size();i++) {
                    JsonObject tool = toolCallsArray.get(i).getAsJsonObject();
                    if(!tool.get("type").getAsString().equals("function")) {
                        continue;
                    }

                    JsonObject functionObject = tool.getAsJsonObject("function");
                    AIModel.Call call = new AIModel.Call();
                    call.setMethodName(functionObject.get("name").getAsString());

                    String argumentsString = functionObject.get("arguments").getAsString();
                    JsonElement elementArguments = JsonParser.parseString(argumentsString);
                    JsonObject argumentsObject = elementArguments.getAsJsonObject();
                    if(argumentsObject != null
                        && !argumentsObject.isJsonNull()) {
                        Set<String> paramNames = argumentsObject.keySet();
                        List<AIModel.CallParam> callParams = new ArrayList<AIModel.CallParam>();
                        for(String paramName: paramNames) {
                            AIModel.CallParam callParam = new AIModel.CallParam();
                            callParam.setName(paramName);
                            callParam.setValue(argumentsObject.get(paramName).getAsString());
                            callParams.add(callParam);
                        }
                        call.setParams(callParams);
                    }
                    calls.add(call);
                }
            }
        }
        return calls; 
    }

    private AIModel.ChatResponse extractChatResponseFromJson(String jsonResponse) throws Exception {
        AIModel.ChatResponse chatResponse = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("choices")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject messageObject = firstChoice.getAsJsonObject("message");
            String content = "";
            if(messageObject.get("content") != null 
                && !messageObject.get("content").isJsonNull()) {
                content = messageObject.get("content").getAsString();
            }

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

    private String generateJsonBodyToGetEmbedding(String model, String input, int dimensions) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("input", input);
        if(dimensions > 0) {
            jsonBody.addProperty("dimensions", dimensions);
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
