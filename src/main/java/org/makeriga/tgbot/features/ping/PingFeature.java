package org.makeriga.tgbot.features.ping;

import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

public class PingFeature extends Feature {

    public static final String FEATURE_ID = "ping";

    public static final String CMD__PING = "ping";

    private String ping(String text) {
        if (text.compareTo("ping") == 0) {
            return "pong";
        }
        return "nepong";
    }

    public PingFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }

    @Override
    public boolean Execute(Update update, boolean isCallback, String text, boolean isPrivateMessage, Long senderId, String senderTitle, Integer messageId, String chatId) {
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
