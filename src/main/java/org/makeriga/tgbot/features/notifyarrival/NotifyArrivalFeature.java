package org.makeriga.tgbot.features.notifyarrival;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.features.occupants.OccupantsFeature;

public class NotifyArrivalFeature extends Feature {
    
    public static final String FEATURE_ID = "notifyarrival";
    
    private static final List<String> ANSWERS__YES = Lists.asList("y", new String[]{"yes", "1", "ja", "jā", "da", "да", "д"});
    private static final List<String> ANSWERS__NO = Lists.asList("n", new String[]{"no", "0", "ne", "nē", "net", "ņet", "нет", "н"});
    private static final List<String> ANSWERS__ABORT = Lists.asList("a", new String[]{"abort", "cancel", "atcelt", "otmena", "отмена", "otmenitj", "otmenit", "отменить"});
    
    private static final long FORM_EXPIRATION_SECONDS = 120;
    
    // Edgars hack
    private static final List<String> ANSWERS__THIS_EVENING = Lists.asList("šovakar", new String[]{"sovakar", "shovakar", "this evening", "evening", "večerom", "vecerom", "vecherom", "вечером"});
    private static final int MAGIC_MINUTES = 777;
    
    private static final String CMD__NOTIFY_ARRIVAL = "/notify_of_arrival";
    
    private static final String PARDON__Q = "Pardon?";
    
    private final Map<Integer, ArrivalNotification> items = new ConcurrentHashMap<>();
    private final Map<Integer, Long> expireDates = new ConcurrentHashMap<>();
    
    private OccupantsFeature occupantsFeature = null;
    
    public NotifyArrivalFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD__NOTIFY_ARRIVAL, "notify about your arrival (private only)");
        
        // reference occupants feature
        occupantsFeature = getBot().getFeatures().containsKey(OccupantsFeature.FEATURE_ID) ? (OccupantsFeature)getBot().getFeatures().get(OccupantsFeature.FEATURE_ID) : null;
    }
    
    @Override
    public boolean Execute(String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        if (!isPrivateMessage && CMD__NOTIFY_ARRIVAL.equals(text) || text.equals(getWrappedCommand(CMD__NOTIFY_ARRIVAL))) {
            sendMessage(chatId, "Private only, please.", messageId);
            return true;
        }        
        
        if (isPrivateMessage && CMD__NOTIFY_ARRIVAL.equals(text)) {
            createForm(senderId, senderTitle);
            ProcessArrivalNotification(chatId, senderId, text, senderTitle);
            return true;
        }
        
        if (isPrivateMessage) {
            return ProcessArrivalNotification(chatId, senderId, text, senderTitle);
        }
        
        return false;
    }
    
    private boolean ProcessArrivalNotification(String chatId, Integer userId, String text, String senderTitle) {
        ArrivalNotification not = items.get(userId);
        if (not == null)
            return false;
        
        if (not.step == ArrivalNotification.STEP__FINISHED)
            return true;
        
        // check if expired
        if (expireDates.containsKey(userId) && expireDates.get(userId) < System.currentTimeMillis()) {
            removeForm(userId);
            return false;
        }
        
        // reset expiration
        expireDates.put(userId, System.currentTimeMillis() + FORM_EXPIRATION_SECONDS * 1000);
        
        do {
            text = text.replaceAll(",", ".").toLowerCase();

            // abort input
            if (ANSWERS__ABORT.contains(text)) {
                removeForm(userId);
                return true;
            }
            
            if (CMD__NOTIFY_ARRIVAL.equals(text)) {} 
            
            // first q
            else if (not.step == ArrivalNotification.STEP__AFTER_HOURS_Q) {
                try {

                    // Edgars hack
                    if (ANSWERS__THIS_EVENING.contains(text)) {
                        text = String.valueOf(MAGIC_MINUTES);
                    }
                    
                    int minutes = (int) (60 * Double.parseDouble(text));

                    // Edgars hack
                    if (minutes == MAGIC_MINUTES * 60) {
                        sendPublicMessage(String.format("%s announces his/her arrival - will arrive \"this evening\". Most likely will come alone.", not.memberName));
                        removeForm(userId);
                        return true;
                    }
                    if (minutes < -30 || minutes > 1440) {
                        throw new Exception();
                    }
                    
                    not.arrivalAfterMinutes = minutes;
                    not.step++;
                } catch (Exception t) {
                    sendMessage(chatId, PARDON__Q, null);
                    return true;
                }
            }
            
            // second q
            else if (not.step == ArrivalNotification.STEP__STAYTIME_Q) {
                try {
                    int minutes = (int) (60 * Double.parseDouble(text));
                    if (minutes < 1 || minutes > 1440) {
                        throw new Exception();
                    }
                    not.stayMinutes = minutes;
                    not.step++;
                } catch (Exception t) {
                    sendMessage(chatId, PARDON__Q, null);
                    return true;
                }
            }
            
            // third q
            else if (not.step == ArrivalNotification.STEP__EXTRA_PERSONS_Q) {
                try {
                    if (ANSWERS__NO.contains(text)) {
                        text = "0";
                    }
                    int extra = Integer.parseInt(text);
                    if (extra < 0 || extra > 5) {
                        throw new Exception();
                    }
                    not.extraMembers = extra;
                    not.step++;
                } catch (Exception t) {
                    sendMessage(chatId, PARDON__Q, null);
                    return true;
                }
                
            }
            // conifirmation
            else if (not.step == ArrivalNotification.STEP__CONFIRMATION) {
                try {
                    boolean confirmation;
                    if (ANSWERS__YES.contains(text)) {
                        confirmation = true;
                    } else if (ANSWERS__NO.contains(text)) {
                        confirmation = false;
                    } else {
                        throw new Exception();
                    }
                    if (!confirmation) {
                        not.step = 1;
                        break;
                    }
                    // send
                    sendPublicMessage(not.toString());
                    
                    if (occupantsFeature != null)
                        occupantsFeature.RegisterArrival(not.arrivalDate, not.leaveDate, not.extraMembers, senderTitle);
                    
                    removeForm(userId);
                    return true;
                } catch (Exception t) {
                    sendMessage(chatId, PARDON__Q, null);
                    return true;
                }
            }
            
        } while (false);
        
        switch (not.step) {
            case ArrivalNotification.STEP__AFTER_HOURS_Q:
                sendMessage(chatId, "After how many hours do you plan to come?", null);
                break;
            
            case ArrivalNotification.STEP__STAYTIME_Q:
                sendMessage(chatId, "How many hours will you stay?", null);
                break;
            
            case ArrivalNotification.STEP__EXTRA_PERSONS_Q:
                sendMessage(chatId, "Extra persons?", null);
                break;
            
            case ArrivalNotification.STEP__CONFIRMATION:
                sendMessage(chatId, "This?\n\n" + not.toString(), null);
                break;
        }
        
        return true;
    }
    
    private void createForm(Integer userId, String userTitle) {
        if (userId == null) {
            return;
        }
        ArrivalNotification not;
        items.put(userId, not = new ArrivalNotification());
        not.memberName = userTitle;
        
        // expire date
        expireDates.put(userId, System.currentTimeMillis() + 60000);
    }
    
    private void removeForm(Integer userId) {
        if (userId == null) {
            return;
        }
        if (!items.containsKey(userId)) {
            return;
        }
        
        try {
            items.get(userId).step = ArrivalNotification.STEP__FINISHED;
            items.remove(userId);
            expireDates.remove(userId);
        } catch (Exception e) { }
    }
    
    @Override
    public String GetId() {
        return FEATURE_ID;
    }
    
}
