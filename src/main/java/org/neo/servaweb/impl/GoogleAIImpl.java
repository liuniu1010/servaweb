package org.neo.servaweb.impl;

import java.util.Map;
import java.util.HashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class GoogleAIImpl extends AbsGoogleAIImpl {
    private DBConnectionIFC dbConnection;

    protected GoogleAIImpl() {
    }

    protected GoogleAIImpl(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
        setup();
    }

    public static GoogleAIImpl getInstance(DBConnectionIFC inputDBConnection) {
        return new GoogleAIImpl(inputDBConnection);
    }

    private static String gemini_1_0_pro = "gemini-1.0-pro";
    private static String gemini_1_5_pro_latest = "gemini-1.5-pro-latest";

    private String[] chatModels;
    private String[] embeddingModels;
    private String[] imageModels;
    private String[] visionModels;

    private Map<String, String> urlMapping;
    private Map<String, Integer> contextWindowMapping;
    private Map<String, Integer> maxOutputMapping;

    private void setup() {
        chatModels = new String[]{gemini_1_5_pro_latest, gemini_1_0_pro};
        embeddingModels = new String[]{};
        imageModels = new String[]{};
        visionModels = new String[]{};

        String apiKey = getApiKey();
        urlMapping = new HashMap<String, String>();
        urlMapping.put(gemini_1_0_pro, "https://generativelanguage.googleapis.com/v1beta/models/" + gemini_1_0_pro + ":generateContent?key=" + apiKey);
        urlMapping.put(gemini_1_5_pro_latest, "https://generativelanguage.googleapis.com/v1beta/models/" + gemini_1_5_pro_latest + ":generateContent?key=" + apiKey);

        contextWindowMapping = new HashMap<String, Integer>();

        maxOutputMapping = new HashMap<String, Integer>();
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
}
