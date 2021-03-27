package org.makeriga.tgbot.features.ping;

import java.io.File;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.telegram.telegrambots.meta.api.objects.Update;

public class PingFeature extends Feature {
    
    public static final String FEATURE_ID = "ping";
    
    public static final String CMD__PING = "ping";
    
    private native String ping(String text);
    
    public PingFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
        System.load(new File(settings.getHomeDirectory(), "features/ping/ping.so").getAbsolutePath());
    }

    @Override
    public boolean Execute(Update update, boolean isCallback, String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        // pong
        if (testCommandWithoutArguments(CMD__PING, text.toLowerCase())) {
            String response = ping(text);
            if (response == null || response.length() > 10)
                return true;
            sendAntispamMessage(chatId, response, !isPrivateMessage ? messageId : null, CMD__PING, senderId);
            return true;
        }

        return false;

    }
    
    @Override
    public String GetId() {
        return FEATURE_ID;
    }

}
