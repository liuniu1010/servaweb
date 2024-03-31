package org.neo.servaweb.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;

public interface OpenAIIFC extends SuperAIIFC {
    public AIModel.Embedding getEmbedding(String model, String input);
    public AIModel.Embedding getEmbedding(String model, String input, int dimensions);
}
