package org.neo.servaweb.impl;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class OpenAIImpl extends AbsOpenAIImpl {
    private static String gpt_35_turbo = "gpt-3.5-turbo";
    private static String gpt_35_turbo_instruct = "gpt-3.5-turbo-instruct";

    private DBConnectionIFC dbConnection;

    public void setDBConnection(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
    }

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

    public String[] getSupportModels() {
        return new String[]{gpt_35_turbo, gpt_35_turbo_instruct};
    }

    protected String getUrl(String model) {
        if(model.equals(gpt_35_turbo)) {
            return "https://api.openai.com/v1/chat/completions";
        }
        else if(model.equals(gpt_35_turbo_instruct)) {
            return "https://api.openai.com/v1/completions";
        }
        else {
            throw new RuntimeException("model " + model + " not supported!");
        }
    }

    protected int getPermitedTokenNumber(String model) {
        return 4096;
    }

    @Override
    protected String getSystemHint() {
        return "You are a helpful assistant.";
    }
}
