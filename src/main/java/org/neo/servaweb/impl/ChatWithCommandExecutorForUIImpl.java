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
import org.neo.servaweb.ifc.ChatForUIIFC;
import org.neo.servaweb.ifc.StorageIFC;
import org.neo.servaweb.ifc.FunctionCallIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class ChatWithCommandExecutorForUIImpl implements ChatForUIIFC, DBQueryTaskIFC, DBSaveTaskIFC {
    private ChatWithCommandExecutorForUIImpl() {
    }

    public static ChatWithCommandExecutorForUIImpl getInstance() {
        return new ChatWithCommandExecutorForUIImpl();
    }

    private StorageIFC storage = null;
    private SuperAIIFC superAI = null;
    private DBConnectionIFC dbConnection = null;
    private FunctionCallIFC functionCallIFC = null;

    private static String standardExceptionMessage = "Exception occurred! Please contact administrator";

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

    // this method should be called by Task
    // to set the environment IFC
    public void setFunctionCall(FunctionCallIFC envFunctionCallIFC) {
        functionCallIFC = envFunctionCallIFC;
    }

    private boolean isEnvironmentReady() {
        return (dbConnection != null) && (dbConnection.isValid());
    }

    protected ChatForUIIFC setupEnvironment(DBConnectionIFC dbConnection) {
        FunctionCallIFC functionCallIFC = CommandCallImpl.getInstance();

        OpenAIForUIImpl openAIForUIImpl = OpenAIForUIImpl.getInstance();
        openAIForUIImpl.setDBConnection(dbConnection);
        SuperAIIFC superAI = openAIForUIImpl;

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);

        ChatWithCommandExecutorForUIImpl chatWithCommandExecutorForUIImpl = ChatWithCommandExecutorForUIImpl.getInstance();
        chatWithCommandExecutorForUIImpl.setSuperAI(superAI);
        chatWithCommandExecutorForUIImpl.setStorage(storage);
        chatWithCommandExecutorForUIImpl.setDBConnection(dbConnection);
        chatWithCommandExecutorForUIImpl.setFunctionCall(functionCallIFC);

        ChatForUIIFC chatForUIIFC = chatWithCommandExecutorForUIImpl;

        return chatForUIIFC;
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
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeSaveTask(new ChatWithCommandExecutorForUIImpl() {
                    @Override
                    public Object save(DBConnectionIFC dbConnection) {
                        ChatForUIIFC chatForUIIFC = super.setupEnvironment(dbConnection);
                        return chatForUIIFC.fetchResponse(session, userInput, attachFiles);
                    }
                });
            }
            else {
                return innerFetchResponse(session, userInput);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String fetchResultFromChatResponse(AIModel.ChatResponse chatResponse) {
        List<AIModel.Call> calls = chatResponse.getCalls();
        if(calls == null
            || calls.size() == 0) {
            return chatResponse.getMessage();
        }
        else {
            AIModel.Call call = calls.get(0);
            String command = call.getParams().get(0).getValue();
            Object runningResult = null;
            try {
                runningResult = functionCallIFC.callFunction(call);
            }
            catch(Exception ex) {
                runningResult = ex.getMessage();
            }
            String result = "$ " + command + "\n" + runningResult;  
            return result;
        }
    }

    private String innerFetchResponse(String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.ChatResponse chatResponse = fetchChatResponse(session, userInput);
        if(chatResponse.getIsSuccess()) {
            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(fetchResultFromChatResponse(chatResponse));
            newResponseRecord.setChatTime(new Date());
 
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
            return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
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
        return superAI.fetchChatResponse(models[0], promptStruct, functionCallIFC);
    }

    @Override
    public String initNewChat(String session) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeSaveTask(new ChatWithCommandExecutorForUIImpl() {
                    @Override
                    public Object save(DBConnectionIFC dbConnection) {
                        ChatForUIIFC chatForUIIFC = super.setupEnvironment(dbConnection);
                        return chatForUIIFC.initNewChat(session);
                    }
                });
            }
            else {
                return innerInitNewChat(session);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerInitNewChat(String session) {
        storage.clearChatRecords(session);

        AIModel.ChatRecord chatRecord = new AIModel.ChatRecord(session);
        chatRecord.setIsRequest(false);
        chatRecord.setChatTime(new Date());
        chatRecord.setContent("Hello, How can I help you?");
        storage.addChatRecord(session, chatRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String refresh(String session) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeQueryTask(new ChatWithCommandExecutorForUIImpl() {
                    @Override
                    public Object query(DBConnectionIFC dbConnection) {
                        ChatForUIIFC chatForUIIFC = super.setupEnvironment(dbConnection);
                        return chatForUIIFC.refresh(session);
                    }
                });
            }
            else {
                return innerRefresh(session);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerRefresh(String session) {
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String echo(String session, String userInput) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeQueryTask(new ChatWithCommandExecutorForUIImpl() {
                    @Override
                    public Object query(DBConnectionIFC dbConnection) {
                        ChatForUIIFC chatForUIIFC = super.setupEnvironment(dbConnection);
                        return chatForUIIFC.echo(session, userInput);
                    }
                });
            }
            else {
                return innerEcho(session, userInput);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerEcho(String session, String userInput) {
        List<AIModel.ChatRecord> chatRecordsInStorage = storage.getChatRecords(session);
 
        List<AIModel.ChatRecord> tmpChatRecords = new ArrayList<AIModel.ChatRecord>();
        tmpChatRecords.addAll(chatRecordsInStorage);

        AIModel.ChatRecord echoRecord = new AIModel.ChatRecord(session);
        echoRecord.setIsRequest(true);
        echoRecord.setChatTime(new Date());
        echoRecord.setContent(userInput);

        tmpChatRecords.add(echoRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(tmpChatRecords, datetimeFormat);
    }
}

