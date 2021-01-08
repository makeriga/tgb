package org.makeriga.tgbot.features.admintools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.features.awards.AwardsFeature;
import org.makeriga.tgbot.helpers.MembersHelper;

public class AdminToolsFeature extends Feature {
    
    public static final String FEATURE_ID = "admintools";

    private static final String CMD__HELP = "help";
    private static final String CMD__LASTUSER = "last";
    private static final String CMD__AWARDS_RESULTS = "god";
    private static final String CMD__WHOIS = "whois";
    private static final String CMD__GENERATE_COMMANDS_DESCRIPTIONS = "cmddescr";
    
    private Map.Entry<String, Long> lastArrived = null;
    private Map.Entry<String, Long> lastMappedArrived = null;
    
    public AdminToolsFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }

    @Override
        public boolean Execute(String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        // PRIVATE - ADMIN COMMANDS
        if (!isPrivateMessage || !settings.getAdminId().equals(senderId))
            return false;
        text = text.toLowerCase();
        
        // notifies admin about last arrived member
        if (testCommandWithoutArguments(CMD__LASTUSER, text)) {
            if (lastMappedArrived == null && lastArrived == null) {
                sendMessage(chatId, "None.", null);
                return true;
            }
            String line1 = lastMappedArrived != null ? String.format("Mapped user: %s (%s) (%s)", lastMappedArrived.getKey(), getBot().getDoorToMembermappings().get(lastMappedArrived.getKey()), Settings.DTF__TEXT.format(new Date(lastMappedArrived.getValue()))) : null;
            String line2 = lastArrived != null ? String.format("Latest user: %s (%s)", lastArrived.getKey(), Settings.DTF__TEXT.format(new Date(lastArrived.getValue()))) : null;

            sendMessage(chatId, String.join("\n", Arrays.asList(new String[]{line1, line2}).stream().filter(i->i != null).collect(Collectors.toList())), null);
            return true;
        }

        if (testCommandWithoutArguments(CMD__HELP, text)) {
            sendMessage(chatId, String.join("\n", CMD__LASTUSER, CMD__AWARDS_RESULTS, CMD__WHOIS, CMD__GENERATE_COMMANDS_DESCRIPTIONS), null);
            return true;
        }

        if (testCommandWithoutArguments(CMD__AWARDS_RESULTS, text)) {
            AwardsFeature feature = (AwardsFeature)getBot().getFeatures().get(AwardsFeature.FEATURE_ID);
            if (feature == null)
                return true;
            File dummyResultsFile = null;
            try {
                dummyResultsFile = File.createTempFile("awards_","dummy");
                dummyResultsFile.delete();
                feature.runAwards(chatId, dummyResultsFile);
            }
            catch (Exception e) { }
            finally {
                if (dummyResultsFile != null)
                    dummyResultsFile.delete();
            }
            return true;
        }
        
        if (testCommandWithArguments(CMD__WHOIS, text)) {
            String query = text.substring(CMD__WHOIS.length() + 1);
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
        
        if (testCommandWithoutArguments(CMD__GENERATE_COMMANDS_DESCRIPTIONS, text)) {
            List<String> commandsDescriptions = new ArrayList<>();
            for (Feature f : getBot().getFeatures().values())
                for (Map.Entry<String, String> command : f.getPublicCommands()) {
                    if (!command.getKey().startsWith("/"))
                        continue;
                    commandsDescriptions.add(String.format("%s - %s", command.getKey().substring(1), command.getValue()));
                }
            
            sendMessage(chatId, commandsDescriptions.isEmpty() ? "None." : String.join("\n", commandsDescriptions), null);
            return true;
        }
        
        return false;

    }
    
    @Override
    public String GetId() {
        return FEATURE_ID;
    }
    
    public void ReportLastUser(Map.Entry<String, Long> item) {
        if (item == null)
            return;
        this.lastArrived = item;
    }
    
    public void ReportLastMappedUser(Map.Entry<String, Long> item) {
        if (item == null)
            return;
        this.lastMappedArrived = item;
    }
    
}
