package org.neo.servaweb.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.neo.servaframe.model.VersionEntity;

public class AIModel {
    public static class ChatRecord {
        public static final String ENTITYNAME = "chatrecord";
        private VersionEntity versionEntity = null;

        public static final String SESSION = "session";
        public static final String CHATTIME = "chattime";
        public static final String CONTENT = "content";
        public static final String ISREQUEST = "isrequest";

        public ChatRecord(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public ChatRecord(String session) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(SESSION, session);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getSession() {
            return (String)versionEntity.getAttribute(SESSION);
        }

        public void setSession(String session) {
            versionEntity.setAttribute(SESSION, session);
        }

        public Date getChatTime() {
            return (Date)versionEntity.getAttribute(CHATTIME);
        }

        public void setChatTime(Date inputChatTime) {
            versionEntity.setAttribute(CHATTIME, inputChatTime);
        }

        public String getContent() {
            return (String)versionEntity.getAttribute(CONTENT);
        }

        public void setContent(String inputContent) {
            versionEntity.setAttribute(CONTENT, inputContent);
        }

        public boolean getIsRequest() {
            return (boolean)versionEntity.getAttribute(ISREQUEST);
        }

        public void setIsRequest(boolean inputIsRequest) {
            versionEntity.setAttribute(ISREQUEST, inputIsRequest);
        }
    }

    public static class PromptStruct {
        private List<AIModel.ChatRecord> chatRecords = new ArrayList<AIModel.ChatRecord>();
        private String userInput;

        public List<AIModel.ChatRecord> getChatRecords() {
            return chatRecords;
        }

        public void setChatRecords(List<AIModel.ChatRecord> inputChatRecords) {
            chatRecords = inputChatRecords;
        }

        public String getUserInput() {
            return userInput;
        }

        public void setUserInput(String inputUserInput) {
            userInput = inputUserInput;
        }
    }

    public static class ChatResponse {
        private boolean isSuccess;
        private String message;   // in case isSuccess is false, message is exception info

        public ChatResponse(boolean inputIsSuccess, String inputMessage) {
            isSuccess = inputIsSuccess;
            message = inputMessage;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class FunctionParam {
        private String name;    // basically, only string type are supported, so no need to define type
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String inputName) {
            name = inputName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String inputDescription) {
            description = inputDescription;
        }
    }

    public static class Function {
        private String methodName;
        private String description;
        private List<FunctionParam> params;
 
        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String inputMethodName) {
            methodName = inputMethodName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String inputDescription) {
            description = inputDescription;
        }

        public List<FunctionParam> getParams() {
            return params;
        }

        public void setParams(List<FunctionParam> inputParams) {
            params = inputParams;
        }
    }

    public static class CallParam {
        private String name;
        private String value;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String inputName) {
            name = inputName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String inputValue) {
            value = inputValue;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String inputDescription) {
            description = inputDescription;
        }
    }

    public static class Call {
        private String methodName;
        private List<CallParam> params;
 
        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String inputMethodName) {
            methodName = inputMethodName;
        }

        public List<CallParam> getParams() {
            return params;
        }

        public void setParams(List<CallParam> inputParams) {
            params = inputParams;
        }
    }
}