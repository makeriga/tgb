package org.makeriga.tgbot.features.notifyarrival;

import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.features.occupants.OccupantsFeature;
import org.telegram.telegrambots.meta.api.objects.Update;

public class NotifyTemporaryVisitFeature extends Feature {

    public static final String FEATURE_ID = "notify_temp_visit";
    private static final String CMD__NOTIFY_TEMPORARY_ARRIVAL = "/notify_temporary_visit";
    
    private OccupantsFeature occupantsFeature = null;

    public NotifyTemporaryVisitFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD__NOTIFY_TEMPORARY_ARRIVAL, "notify about your temporary visit");
        
        // reference occupants feature
        occupantsFeature = getBot().getFeatures().containsKey(OccupantsFeature.FEATURE_ID) ? (OccupantsFeature)getBot().getFeatures().get(OccupantsFeature.FEATURE_ID) : null;
    }
    
    @Override
    public String GetId() {
        return NotifyTemporaryVisitFeature.FEATURE_ID;
    }

    @Override
    public boolean Execute(Update update, boolean isCallback, String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        if (!testCommand(CMD__NOTIFY_TEMPORARY_ARRIVAL, text))
            return false;
        
        ArrivalNotification not = new ArrivalNotification();
        not.arrivalAfterMinutes = 2;
        not.stayMinutes = 5;
        not.memberName = senderTitle;
        
        if (!sendAntispamMessage(settings.getChatId(), not.toString(), null, CMD__NOTIFY_TEMPORARY_ARRIVAL, senderId))
            sendAntispamMessage(chatId, MESSAGE__ANTISPAM_HIT, null, CMD__NOTIFY_TEMPORARY_ARRIVAL + "-resp", senderId);
        else if (isPrivateMessage)
            sendMessage(chatId, "Sent.", null);
                    
        if (occupantsFeature != null)
            occupantsFeature.RegisterArrival(not.arrivalDate, not.leaveDate, not.extraMembers, senderTitle);
        
        return true;
    }
    
}
