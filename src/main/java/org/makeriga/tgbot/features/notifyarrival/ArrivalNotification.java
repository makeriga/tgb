package org.makeriga.tgbot.features.notifyarrival;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.makeriga.tgbot.Settings;

public class ArrivalNotification {
    
    public static final int STEP__FINISHED = 0;
    public static final int STEP__AFTER_HOURS_Q = 1;
    public static final int STEP__STAYTIME_Q = 2;
    public static final int STEP__EXTRA_PERSONS_Q = 3;
    public static final int STEP__CONFIRMATION = 4;
    
    public String memberName;
    public Date referenceDate = new Date();
    public int arrivalAfterMinutes;
    public int stayMinutes;
    public int step = STEP__AFTER_HOURS_Q;
    public int extraMembers;
    
    public Date arrivalDate = null;
    public Date leaveDate = null;
    
    @Override
    public String toString() {
        Calendar c = Calendar.getInstance();
        c.setTime(referenceDate);
        c.add(Calendar.MINUTE, arrivalAfterMinutes);
        arrivalDate = c.getTime();
        c.add(Calendar.MINUTE, stayMinutes);
        leaveDate = c.getTime();
        String announcementPreffixFormat = "%s announces their arrival - ";
        String extraText = extraMembers > 0 ? " Extra " + extraMembers + " pers. will come." : "";
        if (sameDates(arrivalDate, leaveDate, referenceDate))
            return String.format(announcementPreffixFormat + "will stay today from %s to %s.", memberName, Settings.TF__TEXT.format(arrivalDate), Settings.TF__TEXT.format(leaveDate)) + extraText;
        
        return String.format(announcementPreffixFormat + "will arrive at %s; will stay until %s.", memberName, Settings.DTF__TEXT.format(arrivalDate), Settings.DTF__TEXT.format(leaveDate)) + extraText;
    }
    
    public static boolean sameDates(Date... dates) {
        if (dates == null)
            return false;
        if (dates.length < 2)
            return false;
        Set<String> x = new HashSet<>();
        x.addAll(Arrays.asList(dates).stream().map(i->Settings.DF__FILE_NAME.format(i)).collect(Collectors.toList()));
        return x.size() == 1;
    }
}
