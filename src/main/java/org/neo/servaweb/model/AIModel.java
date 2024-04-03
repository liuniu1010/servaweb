package org.neo.servaweb.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.neo.servaframe.model.VersionEntity;

public class AIModel {
    public static class ChatRecord {
        public static final String ENTITYNAME = "chatrecord";
        private VersionEntity versionEntity = null;

        public static final String SESSION = "session";
        public static final String CHATTIME = "chattime";
        public static final String CONTENT = "content";
        public static final String ATTACHMENTGROUP = "attachmentgroup";
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

        public String getAttachmentGroup() {
            return (String)versionEntity.getAttribute(ATTACHMENTGROUP);
        }

        public void setAttachmentGroup(String inputAttachmentGroup) {
            versionEntity.setAttribute(ATTACHMENTGROUP, inputAttachmentGroup);
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
        private AIModel.AttachmentGroup attachmentGroup;

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

        public AIModel.AttachmentGroup getAttachmentGroup() {
            return attachmentGroup;
        }

        public void setAttachmentGroup(AIModel.AttachmentGroup inputAttachmentGroup) {
            attachmentGroup = inputAttachmentGroup;
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

        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "attachmentGroup");
 
            JsonArray jsonArray = new JsonArray();
            for(Attachment attachment: attachments) {
                JsonObject jsonAttachment = attachment.toJsonObject();
                jsonArray.add(jsonAttachment);
            }

            jsonObject.add("attachments", jsonArray);

            return jsonObject;
        }

        public static AttachmentGroup fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("attachmentGroup")) {
                throw new RuntimeException("this jsonObject is not an AttachmentGroup");
            }

            AttachmentGroup attachmentGroup = new AttachmentGroup();
            List<Attachment> attachments = new ArrayList<Attachment>();
            JsonArray jsonArray = jsonObject.getAsJsonArray("attachments");
            for(int i = 0;i < jsonArray.size();i++) {
                Attachment attachment = null;
                JsonObject jsonAttachment = jsonArray.get(i).getAsJsonObject();
                if(jsonAttachment.get("type").getAsString().equals("jpegAsUrl")) {
                    attachment = JpegFileAsUrl.fromJsonObject(jsonAttachment);
                }
                else if(jsonAttachment.get("type").getAsString().equals("jpegAsBase64")) {
                    attachment = JpegFileAsBase64.fromJsonObject(jsonAttachment);
                }
                else if(jsonAttachment.get("type").getAsString().equals("pngAsUrl")) {
                    attachment = PngFileAsUrl.fromJsonObject(jsonAttachment);
                }
                else if(jsonAttachment.get("type").getAsString().equals("pngAsBase64")) {
                    attachment = PngFileAsBase64.fromJsonObject(jsonAttachment);
                }
                else if(jsonAttachment.get("type").getAsString().equals("pdfAsUrl")) {
                    attachment = PdfFileAsUrl.fromJsonObject(jsonAttachment);
                }
                else if(jsonAttachment.get("type").getAsString().equals("pdfAsBase64")) {
                    attachment = PdfFileAsBase64.fromJsonObject(jsonAttachment);
                }

                if(attachment != null) {
                    attachments.add(attachment);
                } 
            }

            attachmentGroup.setAttachments(attachments);
            return attachmentGroup;
        }
    }

    public static interface Attachment {
        abstract public JsonObject toJsonObject();
    }

    abstract public static class AttachmentAsUrl implements Attachment {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String inputUrl) {
            url = inputUrl;
        }
    }

    abstract public static class AttachmentAsBase64 implements Attachment {
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
        @Override
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "jpegAsUrl");
            jsonObject.addProperty("url", getUrl());

            return jsonObject;
        }

        public static JpegFileAsUrl fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("jpegAsUrl")) {
                throw new RuntimeException("this jsonObject is not a JpegAsUrl");
            }

            JpegFileAsUrl jpegFileAsUrl = new JpegFileAsUrl();
            jpegFileAsUrl.setUrl(jsonObject.get("url").getAsString());
            return jpegFileAsUrl;
        }
    }

    public static class JpegFileAsBase64 extends AttachmentAsBase64 implements JpegFile {
        @Override
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "jpegAsBase64");
            jsonObject.addProperty("base64", getBase64());

            return jsonObject;
        }

        public static JpegFileAsBase64 fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("jpegAsBase64")) {
                throw new RuntimeException("this jsonObject is not a JpegAsBase64");
            }

            JpegFileAsBase64 jpegFileAsBase64 = new JpegFileAsBase64();
            jpegFileAsBase64.setBase64(jsonObject.get("base64").getAsString());
            return jpegFileAsBase64;
        }
    }

    public static class PngFileAsUrl extends AttachmentAsUrl implements PngFile {
        @Override
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "pngAsUrl");
            jsonObject.addProperty("url", getUrl());

            return jsonObject;
        }

        public static PngFileAsUrl fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("pngAsUrl")) {
                throw new RuntimeException("this jsonObject is not a PngAsUrl");
            }

            PngFileAsUrl pngFileAsUrl = new PngFileAsUrl();
            pngFileAsUrl.setUrl(jsonObject.get("url").getAsString());
            return pngFileAsUrl;
        }
    }

    public static class PngFileAsBase64 extends AttachmentAsBase64 implements PngFile {
        @Override
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "pngAsBase64");
            jsonObject.addProperty("base64", getBase64());

            return jsonObject;
        }

        public static PngFileAsBase64 fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("pngAsBase64")) {
                throw new RuntimeException("this jsonObject is not a PngAsBase64");
            }

            PngFileAsBase64 pngFileAsBase64 = new PngFileAsBase64();
            pngFileAsBase64.setBase64(jsonObject.get("base64").getAsString());
            return pngFileAsBase64;
        }
    }

    public static class PdfFileAsUrl extends AttachmentAsUrl implements PdfFile {
        @Override
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "pdfAsUrl");
            jsonObject.addProperty("url", getUrl());

            return jsonObject;
        }

        public static PdfFileAsUrl fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("pdfAsUrl")) {
                throw new RuntimeException("this jsonObject is not a PdfAsUrl");
            }

            PdfFileAsUrl pdfFileAsUrl = new PdfFileAsUrl();
            pdfFileAsUrl.setUrl(jsonObject.get("url").getAsString());
            return pdfFileAsUrl;
        }
    }

    public static class PdfFileAsBase64 extends AttachmentAsBase64 implements PdfFile {
        @Override
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "pdfAsBase64");
            jsonObject.addProperty("base64", getBase64());

            return jsonObject;
        }

        public static PdfFileAsBase64 fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("pdfAsBase64")) {
                throw new RuntimeException("this jsonObject is not a pdfAsBase64");
            }

            PdfFileAsBase64 pdfFileAsBase64 = new PdfFileAsBase64();
            pdfFileAsBase64.setBase64(jsonObject.get("base64").getAsString());
            return pdfFileAsBase64;
        }
    }
}
