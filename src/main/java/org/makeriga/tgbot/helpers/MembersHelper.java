package org.makeriga.tgbot.helpers;

import java.io.File;
import java.util.Map;
import org.makeriga.tgbot.Settings;

public class MembersHelper {
    
    private static File stickersDir = null;
    private static File defaultIcon = null;
    
    public static void init(Settings settings) {
        MembersHelper.stickersDir = new File(settings.getHomeDirectory(), "icons");
        MembersHelper.defaultIcon = new File(settings.getHomeDirectory(), "stickers/love.webp");
        assert stickersDir.exists();
        assert defaultIcon.exists();
    }
    
    public static void ParseMembers(Map<String, String> doorToMemberMappings, Map<String, String> memberAlternativeNames) throws Throwable {
        String setting = System.getProperty("users_mappings");
        if (setting == null)
            return;
        
        // DOOR_NAME=REAL_NAME,ALT_NAME_1,ALT_NAME_2
        // Rihards S.=@Skrubis,rihards
        String users[] = setting.split(";");
        for (String item : users) {
            String parts[] = item.split("="); {
                String allNames[] = parts[1].split(",");
                doorToMemberMappings.put(parts[0], allNames[0]);
                for (String name : allNames) {
                    String alt = name.toLowerCase();
                    if (alt.equals(allNames[0]))
                        continue;
                    assert !memberAlternativeNames.containsKey(alt);
                    memberAlternativeNames.put(name.toLowerCase(), allNames[0]);
                }
            }
        }
        
        // @Xxx to @xxx and xxx
        for (String realName : doorToMemberMappings.values()) {
            if (!realName.startsWith("@")) {
                String alt = realName.toLowerCase();
                memberAlternativeNames.put(alt, realName);
                continue;
            }
            
            String alt = realName.toLowerCase();
            if (!memberAlternativeNames.containsKey(alt))
                memberAlternativeNames.put(alt, realName);
            
            alt = alt.substring(1);
            if (!memberAlternativeNames.containsKey(alt))
                memberAlternativeNames.put(alt, realName);
        }
        
        // andis m. => @Aa
        // andis m  => @Aa
        for (String alt : doorToMemberMappings.keySet()) {
            String a = alt.toLowerCase();
            if (!memberAlternativeNames.containsKey(a))
                memberAlternativeNames.put(a, doorToMemberMappings.get(alt));
            
            a = a.substring(0, a.length()-1);
            if (!memberAlternativeNames.containsKey(a))
                memberAlternativeNames.put(a, doorToMemberMappings.get(alt));
        }
    }
    
    public static File getIconFile(Settings settings, String memberName) {
        return getIconFile(settings, memberName, true);
    }
    
    public static File getIconFile(Settings settings, String memberName, boolean returnDefaultIcon) {
        do {
            if (memberName == null)
                break;
            
            if (memberName.length() > 70)
                break;
            
            File stickerFile = new File(stickersDir, memberName + ".webp");
            if (!stickerFile.getParent().equals(stickersDir.getAbsolutePath()))
                break;
            
            if (!stickerFile.exists())
                break;
            
            File altStickerFile = new File(stickersDir, "alt_" + memberName + ".webp");
            
            if (!altStickerFile.exists())
                return stickerFile;
            
            return System.currentTimeMillis() % 2 == 0 ? stickerFile : altStickerFile;
            
        }
        while (false);
        if (returnDefaultIcon)
            return defaultIcon;
        return null;
    }
}
