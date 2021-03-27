package org.makeriga.tgbot.features.occupants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.features.notifyarrival.ArrivalNotification;
import org.telegram.telegrambots.meta.api.objects.Update;

public class OccupantsFeature extends Feature {

    public static final String FEATURE_ID = "occupants";
    private static final String CMD_OCCUPATION = "/occupation";
    private File occupantsFile = null;
    private static final Logger logger = Logger.getLogger(OccupantsFeature.class);

    private final List<Map.Entry<Long, Long>> registeredArrivals = new ArrayList<>();
    private final Map<Map.Entry<Long, Long>, String> arrivalUserTitles = new HashMap<>();
    
    public OccupantsFeature (MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
        occupantsFile = new File(settings.getHomeDirectory(), "features/occupants/current");
        
        // load db
        FromFile();
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD_OCCUPATION, "occupation check");
    }

    @Override
    public boolean Execute(Update update, boolean isCallback, String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {      
        if (!testCommand(CMD_OCCUPATION, text))
            return false;
        
        Integer replyToMessage = isPrivateMessage ? null : messageId;
        String arg = testCommandWithArguments(CMD_OCCUPATION, text) ? text.substring(text.indexOf(" ") + 1).toLowerCase() : null;

        if ("help".equals(arg)) {
            sendMessage(chatId, String.format("usage: %s [time offset in hours] | [todays time]", CMD_OCCUPATION) , replyToMessage);
            return true;
        }
        
        long referenceDate;
        
        do {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            
            double hoursOffset = 0;
            if (arg != null) {
                // parse provided time
                try {
                    LocalTime t = LocalTime.parse(arg);
                    c.set(Calendar.HOUR_OF_DAY, t.getHour());
                    c.set(Calendar.MINUTE, t.getMinute());
                    referenceDate = c.getTime().getTime();
                    break;
                }
                catch (Exception e) {
                    // ignore
                }

                try {
                    arg = arg.replace(",", ".");

                    hoursOffset = Double.parseDouble(arg);
                    if (hoursOffset < -0.5 || hoursOffset > 48)
                        throw new Exception();
                }
                catch (Exception e) {
                    hoursOffset = 0;
                }
            }

            int minutesCount = (int)(hoursOffset * 60);

            // calculate reference date
            c.add(Calendar.MINUTE, minutesCount);
            referenceDate = c.getTimeInMillis();
        }
        while (false);
        
        // count occupants
        List<String> occupantsNames = new ArrayList<>();
        int totalOccupants = 0;
        for (Map.Entry<Long, Long> entry : registeredArrivals) {
            // skip if not effective yet or expired
            if (entry.getKey() > referenceDate || entry.getValue() < referenceDate)
                continue;
            
            totalOccupants++;
            
            // collect unique names to show in the message
            String occupantName = arrivalUserTitles.get(entry);
            if (occupantName == null)
                continue;
            
            if (!occupantsNames.contains(occupantName))
                occupantsNames.add(occupantName);
        }
        
        // choose date format - if same day then time only
        DateFormat df = ArrivalNotification.sameDates(new Date(referenceDate), new Date()) ? Settings.TF__TEXT : Settings.DTF__TEXT;
        String referenceDateText = df.format(new Date(referenceDate));
        
        // when none
        if (totalOccupants == 0) {
            sendMessage(chatId, String.format("No occupation to %s", referenceDateText), replyToMessage);
            return true;
        }
        
        // when some
        sendMessage(chatId, String.format("Occupation to %s: %d pers.", referenceDateText, totalOccupants) + (!occupantsNames.isEmpty() ? " (" + String.join(", ", occupantsNames) + ")" : ""), replyToMessage);
        return true;
    }

    @Override
    public String GetId() {
        return FEATURE_ID;
    }
    
    private void ToFile() {
        try {
            String occupantsText = String.join("\n", registeredArrivals.stream().map(i->String.format("%d-%d=%s", i.getKey(), i.getValue(), arrivalUserTitles.containsKey(i) ? arrivalUserTitles.get(i) : "")).collect(Collectors.toList()));
            FileUtils.writeStringToFile(occupantsFile, occupantsText, Charset.defaultCharset());
        }
        catch (IOException e) {
            // log error
            logger.error("io", e);
        }
    }
    
    private void FromFile() {
        if (!occupantsFile.exists())
            return;
        
        Pattern p = Pattern.compile("^(\\d+?)-(\\d+?)=(.*)$");
        try {

            List<String> lines = FileUtils.readLines(occupantsFile, Charset.defaultCharset());
            for (String line : lines) {
                Matcher m = p.matcher(line);
                if (!m.matches())
                    continue;
                Map.Entry<Long, Long> entry = new AbstractMap.SimpleEntry<>(Long.valueOf(m.group(1)), Long.valueOf(m.group(2)));
                
                // skip invalid items
                if (entry.getKey() >= entry.getValue())
                    continue;
                
                // skip expired items
                if (System.currentTimeMillis() > entry.getValue())
                    continue;
                
                // valid
                registeredArrivals.add(entry);
                if (m.group(3).length() > 0)
                    arrivalUserTitles.put(entry, m.group(3));
            }
            
        }
        catch (IOException e) {
            // log error
            logger.error("io", e);
        }
    }
    
    public void RegisterArrival(Date dateFrom, Date dateTill, int extra, String member) {
        if (dateFrom == null || dateTill == null || dateFrom.after(dateTill))
            return;
        
        for (int i = 0; i<= extra; i++){
            Map.Entry<Long, Long> entry = new AbstractMap.SimpleEntry<>(dateFrom.getTime(), dateTill.getTime());
            registeredArrivals.add(entry);
            arrivalUserTitles.put(entry, member);
        }
        
        // save entries to file
        ToFile();
    }
}
 