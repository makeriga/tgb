package org.makeriga.tgbot.helpers;

public class TgbHelper {

    public static String encodeCallbackData(String featureId, String data) {
        if (data == null)
            return featureId;
        return featureId + "|" + data;
    }
    
    public static CallbackData decodeCallbackData(String rawData) {
        if (rawData == null)
            return new CallbackData(null);
        if (!rawData.contains("|"))
            return new CallbackData(rawData);
        int index = rawData.indexOf("|");
        return new CallbackData(rawData.substring(0, index), rawData.substring(index + 1));
    }
    
    public static class CallbackData {
        
        public CallbackData(String data) {
            this(null, data);
        }
        
        public CallbackData(String featureId, String data) {
            this.featureId = featureId;
            this.data = data;
        }
        
        private String featureId;
        private String data;

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
    }
}
