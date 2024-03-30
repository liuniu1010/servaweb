package org.neo.servaweb.impl;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class OpenAIForUIImpl extends OpenAIImpl {
    private OpenAIForUIImpl() {
    }
 
    public static OpenAIForUIImpl getInstance() {
        return new OpenAIForUIImpl();
    }

    @Override
    protected String getSystemHint() {
        return "You are a helpful assistant. You always try to response result in a formal format, maybe including some html elements to make it better shown.";
    }
}
