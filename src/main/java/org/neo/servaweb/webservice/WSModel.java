package org.neo.servaweb.webservice;

public class WSModel {
    public static class AIChatParams {
        String loginSession;
        String userInput;
        String fileAsBase64;

        public String getLoginSession() {
            return loginSession;
        }

        public void setLoginSession(String inputLoginSession) {
            loginSession = inputLoginSession;
        }

        public String getUserInput() {
            return (userInput == null)?"":userInput;
        }

        public void setUserInput(String inputUserInput) {
            userInput = inputUserInput;
        }

        public String getFileAsBase64() {
            return (fileAsBase64 == null)?"":fileAsBase64;
        }

        public void setFileAsBase64(String inputFileAsBase64) {
            fileAsBase64 = inputFileAsBase64;
        }
    }

    public static class AIChatResponse {
        private boolean isSuccess;
        private String message;

        public AIChatResponse(boolean inputIsSuccess, String inputMessage) {
            isSuccess = inputIsSuccess;
            message = inputMessage;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public void setIsSuccess(boolean inputIsSuccess) {
            isSuccess = inputIsSuccess;
        }

        public String getMessage() {
            return (message == null)?"":message;
        }

        public void setMessage(String inputMessage) {
            message = inputMessage;
        }
    }
}
