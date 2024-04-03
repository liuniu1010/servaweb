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

    public static class ImagePrompt {
        private String userInput;
        private String size = "1024x1024";   // default as 1024x1024
        private int number = 1; // default as 1

        public String getUserInput() {
            return userInput;
        }

        public void setUserInput(String inputUserInput) {
            userInput = inputUserInput;
        }
       
        public String getSize() {
            return size;
        }

        public void setSize(String inputSize) {
            size = inputSize;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int inputNumber) {
            number = inputNumber;
        }
    }

    public static class ChatResponse {
        private boolean isSuccess;
        private String message;   // in case isSuccess is false, message is exception info
        private List<AIModel.Call> calls;

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

        public List<AIModel.Call> getCalls() {
            return calls;
        }

        public void setCalls(List<AIModel.Call> inputCalls) {
            calls = inputCalls;
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

        @Override
        public String toString() {
            String str = "paramName = " + name;
            str += "\nparamValue = " + value;
            return str;
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

        @Override
        public String toString() {
            String str = "methodName = " + methodName;
            if(params != null) {
                for(CallParam param: params) {
                    str += "\n" + param.toString();
                }
            }
            return str;
        }
    }

    public static class Embedding {
        private double[] data;

        public Embedding(double[] inputData) {
            data = inputData;
        }

        public int size() {
            return (data == null)?0:data.length;
        }

        public double get(int index) {
            return data[index];
        }

        @Override
        public String toString() {
            String str = "[";
            for(int i = 0;i < this.size();i++) {
                if(i > 0) {
                    str += ", ";
                }
                str += data[i];
            }
            str += "]";

            return str;
        }
    }

    public static class AttachmentGroup {
        List<Attachment> attachments;

        public List<Attachment> getAttachment() {
            return attachments;
        }

        public void setAttachments(List<Attachment> inputAttachments) {
            attachments = inputAttachments;
        }
    }

    public static interface Attachment {
    }

    abstract static class AttachmentAsUrl implements Attachment {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String inputUrl) {
            url = inputUrl;
        }
    }

    abstract static class AttachmentAsBase64 implements Attachment {
        private String base64;

        public String getBase64() {
            return base64;
        }

        public void setBase64(String inputBase64) {
            base64 = inputBase64;
        }
    }

    public static interface JpegFile extends Attachment {
    }

    public static interface PngFile extends Attachment {
    }

    public static interface PdfFile extends Attachment {
    }

    public static class JpegFileAsUrl extends AttachmentAsUrl implements JpegFile {
    }

    public static class JpegFileAsBase64 extends AttachmentAsBase64 implements JpegFile {
    }

    public static class PngFileAsUrl extends AttachmentAsUrl implements PngFile {
    }

    public static class PngFileAsBase64 extends AttachmentAsBase64 implements PngFile {
    }

    public static class PdfFileAsUrl extends AttachmentAsUrl implements PdfFile {
    }

    public static class PdfFileAsBase64 extends AttachmentAsBase64 implements PdfFile {
    }
}
