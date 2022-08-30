package org.makeriga.tgbot.features.lovingit;

import java.io.File;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.telegram.telegrambots.meta.api.objects.Update;

public class LovingItFeature extends Feature {
    
    public static final String FEATURE_ID = "lovingit";

    public final File lovingItStickerFile = new File(new File(settings.getHomeDirectory(), "stickers"), "love.webp");
    
    public LovingItFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }

    @Override
    public boolean Execute(Update update, boolean isCallback, String text, boolean isPrivateMessage, Long senderId, String senderTitle, Integer messageId, String chatId) {
        if (text == null)
            return false;
        // response to mention - question like messages
        if (text.contains(settings.getBotUsername()) && text.length() > 16 && text.substring(text.length() - 7).contains("?")) {
            if (!getBot().TestRequestRate("bot-q-"+chatId))
                return true;
            sendSticker(chatId, messageId, lovingItStickerFile);
            return true;
        }
        return false;
    }
    
    @Override
    public String GetId() {
        return FEATURE_ID;
    }
    
}
