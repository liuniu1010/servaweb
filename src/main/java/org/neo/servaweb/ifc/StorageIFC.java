package org.neo.servaweb.ifc;

import java.util.List;
import org.neo.servaweb.model.AIModel;

public interface StorageIFC {
    public List<AIModel.ChatRecord> getChatRecords(Object key);
    public void addChatRecord(Object key, AIModel.ChatRecord chatRecord);
    public void clearChatRecords(Object key);
}
