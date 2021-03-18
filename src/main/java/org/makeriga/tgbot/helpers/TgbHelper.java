package org.makeriga.tgbot.helpers;

public class TgbHelper {
    
    private static int FEATURE__AUTODELETE_MESSAGE = 1;
    private static int FEATURE__AUTODELETE_KEYBOARD = 2;
    
    public static String encodeCallbackData(String featureId, String data, boolean autodeleteInlineKeyboard, boolean autodeleteMessage) {
        long features = (autodeleteInlineKeyboard ? FEATURE__AUTODELETE_KEYBOARD : 0) | (autodeleteInlineKeyboard ? FEATURE__AUTODELETE_MESSAGE : 0);
        String preffix = features > 0 ? String.format("%d-", features) : "";
        if (data == null)
            return preffix + featureId;
        return preffix + featureId + "|" + data;
    }
    
    public static CallbackData decodeCallbackData(String rawData) {
        if (rawData == null)
            return new CallbackData(null, false, false);
        long features = 0;
        if (rawData.matches("^\\d+-.+?")) {
            int index = rawData.indexOf("-");
            features = Long.valueOf(rawData.substring(0, index));
            rawData = rawData.substring(index + 1);
        }
        
        boolean autodeleteMessage = (features & FEATURE__AUTODELETE_MESSAGE) > 0;
        boolean autodeleteInlineKeyboard = (features & FEATURE__AUTODELETE_KEYBOARD) > 0;
        if (!rawData.contains("|"))
            return new CallbackData(rawData, autodeleteMessage, autodeleteInlineKeyboard);
        int index = rawData.indexOf("|");
        return new CallbackData(rawData.substring(0, index), rawData.substring(index + 1), autodeleteMessage, autodeleteInlineKeyboard);
    }
    
    public static class CallbackData {
        
        public CallbackData(String data, boolean autodeleteMessage, boolean autodeleteInlineKeyboard) {
            this(null, data, autodeleteMessage, autodeleteInlineKeyboard);
        }
        
        public CallbackData(String featureId, String data, boolean autodeleteMessage, boolean autodeleteInlineKeyboard) {
            this.featureId = featureId;
            this.data = data;
            this.autodeleteMessage = autodeleteMessage;
            this.autodeleteInlineKeyboard = autodeleteInlineKeyboard;
        }
        
        private String featureId;
        private String data;
        private boolean autodeleteMessage;
        private boolean autodeleteInlineKeyboard;

        public String getFeatureId() {
            return featureId;
        }

        public void setFeatureId(String featureId) {
            this.featureId = featureId;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
        
        public boolean isAutodeleteInlineKeyboard() {
            return autodeleteInlineKeyboard;
        }
        
        public void setAutodeleteInlineKeyboard(boolean autodeleteInlineKeyboard) {
            this.autodeleteInlineKeyboard = autodeleteInlineKeyboard;
        }
        
        public boolean isAutodeleteMessage() {
            return autodeleteMessage;
        }
        
        public void setAutodeleteMessage(boolean autodeleteMessage) {
            this.autodeleteMessage = autodeleteMessage;
        }
    }
}
