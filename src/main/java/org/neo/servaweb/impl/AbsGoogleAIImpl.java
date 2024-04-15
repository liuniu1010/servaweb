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
import org.neo.servaweb.ifc.GoogleAIIFC;
import org.neo.servaweb.ifc.FunctionCallIFC;
import org.neo.servaweb.model.AIModel;

abstract public class AbsGoogleAIImpl implements GoogleAIIFC {
    final static Logger logger = Logger.getLogger(AbsGoogleAIImpl.class);

    abstract protected String getApiKey();
    abstract protected String getUrl(String model);

    @Override
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct) {
        try {
            return innerFetchChatResponse(model, promptStruct, null);
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
            return innerFetchChatResponse(model, promptStruct, functionCallIFC);
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
            return innerGetEmbedding(model, input, -1);
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
            return innerGetEmbedding(model, input, dimensions);
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
    public String[] generateImages(String model, AIModel.ImagePrompt imagePrompt) {
        try {
            return innerGenerateImage(model, imagePrompt);
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

    private String[] innerGenerateImage(String model, AIModel.ImagePrompt imagePrompt) throws Exception {
        String jsonInput = generateJsonBodyToGenerateImage(model, imagePrompt);
        String jsonResponse = send(model, jsonInput);
        String[] urls = extractImageUrlsFromJson(jsonResponse);
        return urls;
    }

    private AIModel.Embedding innerGetEmbedding(String model, String input, int dimensions) throws Exception {
        String jsonInput = generateJsonBodyToGetEmbedding(model, input, dimensions);
        String jsonResponse = send(model, jsonInput);
        AIModel.Embedding embedding = extractEmbeddingFromJson(jsonResponse);
        return embedding;
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        String jsonInput = generateJsonBodyToFetchResponse(model, promptStruct, functionCallIFC);
        String jsonResponse = send(model, jsonInput);
        List<AIModel.Call> calls = extractCallsFromJson(jsonResponse);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        chatResponse.setCalls(calls);
        return chatResponse;
    }

    private AIModel.Embedding extractEmbeddingFromJson(String jsonResponse) {
        // to be implemented
        return null;
    }

    private String[] extractImageUrlsFromJson(String jsonResponse) {
        // to be implemented
        return null;
    }

    private List<AIModel.Call> extractCallsFromJson(String jsonResponse) throws Exception {
        List<AIModel.Call> calls = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("candidates")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("candidates");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject contentObject = firstChoice.getAsJsonObject("content");
            JsonArray partsArray = contentObject.getAsJsonArray("parts");
            JsonObject firstPart = partsArray.get(0).getAsJsonObject();

            if(firstPart.get("functionCall") == null
                || firstPart.get("functionCall").isJsonNull()) {
                return null;
            }

            calls = new ArrayList<AIModel.Call>();
            JsonObject functionCall = firstPart.getAsJsonObject("functionCall");
            AIModel.Call call = new AIModel.Call();
            call.setMethodName(functionCall.get("name").getAsString());

            JsonObject argumentsObject = functionCall.getAsJsonObject("args");
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

        return calls;
    }

    private AIModel.ChatResponse extractChatResponseFromJson(String jsonResponse) throws Exception {
        AIModel.ChatResponse chatResponse = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("candidates")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("candidates");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject contentObject = firstChoice.getAsJsonObject("content");
            JsonArray partsArray = contentObject.getAsJsonArray("parts");
            JsonObject firstPart = partsArray.get(0).getAsJsonObject();
            String text = "";
            if(firstPart.get("text") != null
                && !firstPart.get("text").isJsonNull()) {
                text = firstPart.get("text").getAsString();
            }

            chatResponse = new AIModel.ChatResponse(true, text);
        }
        else {
            String errorMessage = jsonObject.getAsJsonObject("error").get("message").getAsString();
            chatResponse = new AIModel.ChatResponse(false, errorMessage);
        }
        return chatResponse;
    }

    private JsonObject generateJsonObjectFromChatRecord(AIModel.ChatRecord chatRecord) {
        JsonArray jsonParts = new JsonArray();
        JsonObject jsonText = new JsonObject();
        jsonText.addProperty("text", chatRecord.getContent());
        jsonParts.add(jsonText);

        JsonObject recordContent = new JsonObject();
        recordContent.addProperty("role", chatRecord.getIsRequest()?"user":"model");
        recordContent.add("parts", jsonParts);

        return recordContent;
    }

    private JsonArray generateJsonArrayContents(String model, AIModel.PromptStruct promptStruct) {
        JsonArray jsonContents = new JsonArray();
        
        List<AIModel.ChatRecord> chatRecords = promptStruct.getChatRecords();

        // conversation history parts
        boolean beginWithUserRole = false;
        for(AIModel.ChatRecord chatRecord: chatRecords) {
            // for google api request, the first and last end text should be role user
            // so the first end text with role model should be filtered out
            if(chatRecord.getIsRequest()) {
                beginWithUserRole = true;
            }
            
            if(beginWithUserRole) {
                jsonContents.add(generateJsonObjectFromChatRecord(chatRecord));
            }
        }

        // user input part
        JsonArray jsonUserParts = new JsonArray();
        JsonObject jsonUserText = new JsonObject();
        jsonUserText.addProperty("text", promptStruct.getUserInput());
        jsonUserParts.add(jsonUserText);

        JsonObject userInputContent = new JsonObject();
        userInputContent.addProperty("role", "user");
        userInputContent.add("parts", jsonUserParts);

        jsonContents.add(userInputContent);

        return jsonContents;
    }


    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        JsonArray jsonContents = generateJsonArrayContents(model, promptStruct);
        jsonBody.add("contents", jsonContents);

        if(functionCallIFC != null) {
            JsonArray tools = generateJsonArrayTools(functionCallIFC);
            jsonBody.add("tools", tools);
        }

        return gson.toJson(jsonBody);
    }

    private JsonArray generateJsonArrayTools(FunctionCallIFC functionCallIFC) {
        JsonArray tools = new JsonArray();

        List<AIModel.Function> functions = functionCallIFC.getFunctions();
        for(AIModel.Function function: functions) {
            JsonObject jsonFunctionDeclaration = new JsonObject();
            jsonFunctionDeclaration.addProperty("name", function.getMethodName());
            jsonFunctionDeclaration.addProperty("description", function.getDescription());

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
            jsonFunctionDeclaration.add("parameters", jsonParameters);

            JsonObject jsonTool = new JsonObject();
            jsonTool.add("functionDeclarations", jsonFunctionDeclaration);

            tools.add(jsonTool);
        }

        return tools;
    }

    private String generateJsonBodyToGetEmbedding(String model, String input, int dimensions) {
        // to be implemented
        return null;
    }

    private String generateJsonBodyToGenerateImage(String model, AIModel.ImagePrompt imagePrompt) {
        // to be implemented
        return null;
    }

    private String send(String model, String jsonInput) throws Exception {
        logger.debug("call googleai api, model = " + model + ", jsonInput = " + jsonInput);
        URL url = new URL(getUrl(model));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()){
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            try (InputStream in = connection.getInputStream()){
                String response = IOUtil.inputStreamToString(in);
                logger.debug("return from googleai api, response = " + response);
                return response;
            }
        }
        finally {
            connection.disconnect();
        }
    }
}
