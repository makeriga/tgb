package org.makeriga.tgbot.features.whois;

import java.io.File;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.helpers.MembersHelper;

public class WhoisFeature extends Feature {
    
    public static final String FEATURE_ID = "whois";
    
    public static final String CMD__WHOIS = "/whois";

    public WhoisFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD__WHOIS, "identifies a member");
    }

    @Override
    public boolean Execute(String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        if (!testCommandWithArguments(CMD__WHOIS, text))
            return false;
        
        String query = text.substring(text.indexOf(" ")  + 1).toLowerCase();
        if (query.length() < 1)
            return true;
        do {
            String realName = getBot().getRealName(query);
            if (realName == null)
                break;
            // send nickname
            sendMessage(chatId, realName, null);
            File f = MembersHelper.getIconFile(settings, realName, false);
            if (f == null)
                return true;
            sendSticker(chatId, null, f);
            return true;

        }
        while (false);

        sendMessage(chatId, "I don't know.", null);
        return true;
    }

    @Override
    public String GetId() {
        return FEATURE_ID;
    }
    
}
