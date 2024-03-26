package org.neo.servaweb.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;

public interface OpenAIIFC extends SuperAIIFC {
    public AIModel.ChatResponse fetchChatResponseWithFunctionCall(String inputModel, AIModel.PromptStruct inputPromptStruct, FunctionCallIFC functionCallIFC);
}
