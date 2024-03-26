package org.neo.servaweb.ifc;

import java.util.List;
import org.neo.servaweb.model.AIModel;

public interface FunctionCallIFC {
    public List<AIModel.Function> getFunctions();
    public Object callFunction(AIModel.Call call);
}
