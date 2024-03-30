package org.neo.servaweb.impl;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.ServiceFactory;
import org.neo.servaweb.ifc.ChatForUIIFC;
import org.neo.servaweb.ifc.StorageIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class ExecuteCommandForUIImpl implements ChatForUIIFC, DBQueryTaskIFC, DBSaveTaskIFC {
    private StorageIFC storage = null;
    private DBConnectionIFC dbConnection = null;

    private static String standardExceptionMessage = "Exception occurred! Please contact administrator";

    // this method should be called by Task
    // to set the environment IFC
    public void setStorage(StorageIFC envStorage) {
        storage = envStorage;
    }

    // this method should be called by Task
    // to set the environment IFC
    public void setDBConnection(DBConnectionIFC envDBConnection) {
        dbConnection = envDBConnection;
    }

    private boolean isEnvironmentReady() {
        return (dbConnection != null) && (dbConnection.isValid());
    }

    private ExecuteCommandForUIImpl() {
    }

    public static ExecuteCommandForUIImpl getInstance() {
        return new ExecuteCommandForUIImpl();
    }

    protected ChatForUIIFC setupEnvironment(DBConnectionIFC dbConnection) {
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);

        ExecuteCommandForUIImpl executeCommandForUIImpl = ExecuteCommandForUIImpl.getInstance();
        executeCommandForUIImpl.setStorage(storage);
        executeCommandForUIImpl.setDBConnection(dbConnection);

        ChatForUIIFC chatForUIIFC = executeCommandForUIImpl;

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
    public String fetchResponse(String session, String userInput) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeSaveTask(new ExecuteCommandForUIImpl() {
                    @Override
                    public Object save(DBConnectionIFC dbConnection) {
                        ChatForUIIFC chatForUIIFC = super.setupEnvironment(dbConnection);
                        return chatForUIIFC.fetchResponse(session, userInput);
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

    private String innerFetchResponse(String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        String runningResult = "";
        try {
            runningResult = CommonUtil.executeCommand(userInput);
        }
        catch(Exception ex) {
            runningResult = ex.getMessage();
        }

        String result = "$ " + userInput + "\n" + runningResult;
        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setIsRequest(false);
        newResponseRecord.setContent(result);
        newResponseRecord.setChatTime(new Date());

        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    private AIModel.ChatResponse fetchChatResponse(String session, String userInput) {
        String command = userInput;
        try {
            String runningResult = CommonUtil.executeCommand(command);
            String result = "$ " + command + "\n" + runningResult;
            return new AIModel.ChatResponse(true, result);
        }
        catch(Exception ex) {
            String runningResult = ex.getMessage();
            String result = "$ " + command + "\n" + runningResult;
            return new AIModel.ChatResponse(false, result);
        }
    }

    @Override
    public String initNewChat(String session) {
        try {
            if(!isEnvironmentReady()) {
                DBServiceIFC dbService = ServiceFactory.getDBService();
                return (String)dbService.executeSaveTask(new ExecuteCommandForUIImpl() {
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
                return (String)dbService.executeQueryTask(new ExecuteCommandForUIImpl() {
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
                return (String)dbService.executeQueryTask(new ExecuteCommandForUIImpl() {
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
