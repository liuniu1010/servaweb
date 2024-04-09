package org.neo.servaweb.impl;

import java.util.Map;
import java.util.HashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class GoogleAIImpl extends AbsGoogleAIImpl {
    protected GoogleAIImpl() {
        setup();
    }

    public static GoogleAIImpl getInstance() {
        return new GoogleAIImpl();
    }

    private static String gemini_pro = "gemini-pro";

    private String[] chatModels;
    private String[] embeddingModels;
    private String[] imageModels;
    private String[] visionModels;

    private Map<String, String> urlMapping;
    private Map<String, Integer> contextWindowMapping;
    private Map<String, Integer> maxOutputMapping;

    private void setup() {
        chatModels = new String[]{gemini_pro};
        embeddingModels = new String[]{};
        imageModels = new String[]{};
        visionModels = new String[]{};

        String apiKey = getApiKey();
        urlMapping = new HashMap<String, String>();
        urlMapping.put(gemini_pro, "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey);

        contextWindowMapping = new HashMap<String, Integer>();

        maxOutputMapping = new HashMap<String, Integer>();
    }

    private DBConnectionIFC dbConnection;

    public void setDBConnection(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
    }

    @Override
    protected String getApiKey() {
        try {
            return CommonUtil.getConfigValue(dbConnection, "GoogleApiKey");
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String[] getChatModels() {
        return chatModels;
    }

    @Override
    public String[] getEmbeddingModels() {
        return embeddingModels;
    }

    @Override
    public String[] getImageModels() {
        return imageModels;
    }

    @Override
    public String[] getVisionModels() {
        return visionModels;
    }

    @Override
    protected String getUrl(String model) {
        if(urlMapping.containsKey(model)) {
            return urlMapping.get(model);
        }
        else {
            throw new RuntimeException("model " + model + " not supported to get url!");
        }
    }

    @Override
    protected int getContextWindow(String model) {
        if(contextWindowMapping.containsKey(model)) {
            return contextWindowMapping.get(model);
        }
        else {
            throw new RuntimeException("model " + model + " not supported to get context window!");
        }
    }

    @Override
    protected int getMaxOutputTokenNumber(String model) {
        if(maxOutputMapping.containsKey(model)) {
            return maxOutputMapping.get(model);
        }
        else {
            throw new RuntimeException("model " + model + " not supported to get max output tokens!");
        }
    }

    @Override
    protected String getSystemHint() {
        return "You are a helpful assistant. You always response result in plain text.";
    }
}
