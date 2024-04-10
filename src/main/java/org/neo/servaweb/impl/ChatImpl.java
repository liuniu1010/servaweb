package org.neo.servaweb.impl;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.ServiceFactory;
import org.neo.servaweb.ifc.SuperAIIFC;
import org.neo.servaweb.ifc.ChatIFC;
import org.neo.servaweb.ifc.StorageIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class ChatImpl implements ChatIFC, DBQueryTaskIFC, DBSaveTaskIFC {
    private ChatImpl() {
    }

    public static ChatImpl getInstance() {
        return new ChatImpl();
    }

    private StorageIFC storage = null;
    private SuperAIIFC superAI = null;
    private DBConnectionIFC dbConnection = null;

    // this method should be called by Task
    // to set the environment IFC
    public void setStorage(StorageIFC envStorage) {
        storage = envStorage;
    }

    // this method should be called by Task
    // to set the environment IFC
    public void setSuperAI(SuperAIIFC envSuperAI) {
        superAI = envSuperAI;
    }

    // this method should be called by Task
    // to set the environment IFC
    public void setDBConnection(DBConnectionIFC envDBConnection) {
        dbConnection = envDBConnection;
    }

    private boolean isEnvironmentReady() {
        return (dbConnection != null) && (dbConnection.isValid());
    }

    protected ChatIFC setupEnvironment(DBConnectionIFC dbConnection) {
        OpenAIImpl openAIImpl = OpenAIImpl.getInstance(dbConnection);
        SuperAIIFC superAI = openAIImpl;

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);

        ChatImpl chatImpl = ChatImpl.getInstance();
        chatImpl.setSuperAI(superAI);
        chatImpl.setStorage(storage);
        chatImpl.setDBConnection(dbConnection);
        ChatIFC chatIFC = chatImpl;

        return chatIFC;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        return null;
    }


    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String fetchResponse(String session, String userInput) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeSaveTask(new ChatImpl() {
                    @Override
                    public Object save(DBConnectionIFC dbConnection) {
                        ChatIFC chatIFC = super.setupEnvironment(dbConnection);
                        return chatIFC.fetchResponse(session, userInput);
                    }
                });
            }
            else {
                return innerFetchResponse(session, userInput);
            }
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String innerFetchResponse(String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);

        AIModel.ChatResponse chatResponse = fetchChatResponse(session, userInput);
        if(chatResponse.getIsSuccess()) {
            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(chatResponse.getMessage());

            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            return chatResponse.getMessage();
        }
        else {
            throw new RuntimeException(chatResponse.getMessage());
        }
    }

    private AIModel.ChatResponse fetchChatResponse(String session, String userInput) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(userInput);

        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    }

    @Override
    public void initNewChat(String session) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                dbService.executeSaveTask(new ChatImpl() {
                    @Override
                    public Object save(DBConnectionIFC dbConnection) {
                        ChatIFC chatIFC = super.setupEnvironment(dbConnection);
                        chatIFC.initNewChat(session);
                        return null;
                    }
                });
            }
            else {
                innerInitNewChat(session);
            }
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void innerInitNewChat(String session) {
        storage.clearChatRecords(session);
    }
}
