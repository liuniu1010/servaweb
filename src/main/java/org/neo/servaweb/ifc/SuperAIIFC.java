package org.neo.servaweb.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;

public interface SuperAIIFC {
    public String[] getSupportModels();
    public AIModel.ChatResponse fetchChatResponse(String inputModel, AIModel.PromptStruct inputPromptStruct);
    public AIModel.ChatResponse fetchChatResponse(String inputModel, AIModel.PromptStruct inputPromptStruct, FunctionCallIFC functionCallIFC);
}
