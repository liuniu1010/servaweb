package org.neo.servaweb.impl;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class OpenAIImpl extends AbsOpenAIImpl {
    protected OpenAIImpl() {
    }

    public static OpenAIImpl getInstance() {
        return new OpenAIImpl();
    }

    private static String gpt_4_turbo_preview = "gpt-4-turbo-preview";
    private static String gpt_35_turbo = "gpt-3.5-turbo";
    private static String text_embedding_3_large = "text-embedding-3-large";
    private static String text_embedding_3_small = "text-embedding-3-small";
    private static String dall_e_3 = "dall-e-3";
    private static String dall_e_2 = "dall-e-2";

    private DBConnectionIFC dbConnection;

    public void setDBConnection(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
    }

    @Override
    protected String getApiKey() {
        try {
            return CommonUtil.getConfigValue(dbConnection, "OpenAiApiKey");
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
        return new String[]{gpt_4_turbo_preview, gpt_35_turbo};
    }

    @Override
    public String[] getEmbeddingModels() {
        return new String[]{text_embedding_3_large, text_embedding_3_small};
    }

    @Override
    public String[] getImageModels() {
        return new String[]{dall_e_3, dall_e_2};
    }

    @Override
    protected String getUrl(String model) {
        if(model.equals(gpt_4_turbo_preview)
            || model.equals(gpt_35_turbo)) {
            return "https://api.openai.com/v1/chat/completions";
        }
        else if(model.equals(text_embedding_3_large)
            || model.equals(text_embedding_3_small)) {
            return "https://api.openai.com/v1/embeddings";
        }
        else if(model.equals(dall_e_3)
            || model.equals(dall_e_2)) {
            return "https://api.openai.com/v1/images/generations";
        }
        else {
            throw new RuntimeException("model " + model + " not supported!");
        }
    }

    @Override
    protected int getContextWindow(String model) {
        if(model.equals(gpt_4_turbo_preview)) {
            return 128000;
        }
        else if(model.equals(gpt_35_turbo)) {
            return 16385;
        }
        return -1;
    }

    @Override
    protected int getMaxOutputTokenNumber(String model) {
        if(model.equals(gpt_4_turbo_preview)) {
            return 4096;
        }
        else if(model.equals(gpt_35_turbo)) {
            return 4096;
        }
        return -1;
    }

    @Override
    protected String getSystemHint() {
        return "You are a helpful assistant. You always response result in plain text.";
    }
}
