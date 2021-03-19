package org.makeriga.tgbot.features.notifyarrival;

import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;

public class NotifyTemporaryVisitFeature extends Feature {

    public static final String FEATURE_ID = "notify_temp_visit";
    private static final String CMD__NOTIFY_TEMPORARY_ARRIVAL = "/notify_temporary_visit";

    public NotifyTemporaryVisitFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD__NOTIFY_TEMPORARY_ARRIVAL, "notify about your temporary visit (private only)");
    }
    
    @Override
    public String GetId() {
        return NotifyTemporaryVisitFeature.FEATURE_ID;
    }

    @Override
    public boolean Execute(boolean isCallback, String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        if (!testCommand(CMD__NOTIFY_TEMPORARY_ARRIVAL, text))
            return false;
        
        if (!isPrivateMessage && testCommandWithoutArguments(CMD__NOTIFY_TEMPORARY_ARRIVAL, text)) {
            sendMessage(chatId, "Private only, please.", messageId);
            return true;
        }
        
        ArrivalNotification notif = new ArrivalNotification();
        notif.arrivalAfterMinutes = 5;
        notif.stayMinutes = 5;
        notif.memberName = senderTitle;
        sendMessage(chatId, notif.toString(), null);
        
        return true;
    }
    
}
