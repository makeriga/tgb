package org.makeriga.tgbot.features.awards;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.features.lovingit.LovingItFeature;
import org.makeriga.tgbot.helpers.MembersHelper;

public class AwardsFeature extends Feature {

    public static final String FEATURE_ID = "awards";
    private static final String AWARDS_MESSAGE_FORMAT = "%s, you've served our community and you did it well. God bless you and we love you, %s.";

    private static final Logger logger = Logger.getLogger(AwardsFeature.class);

    private static final String CMD__VOTE = "/vote";

    private static final Object VOTES_LOCK = new Object();
    private final File votesDirectory = new File(settings.getHomeDirectory(), "votes");
    private final File currentVotesFile = new File(votesDirectory, "current");
    private final File iconsDirectory = new File(settings.getHomeDirectory(), "icons");
    private File lovingItStickerFile = null;

    public AwardsFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);

        // schedule awards ceremony
        scheduleNextAwardCeremony();
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD__VOTE, "give award to member");
        
        LovingItFeature f = (LovingItFeature)getBot().getFeatures().get(LovingItFeature.FEATURE_ID);
        assert (f != null);
        this.lovingItStickerFile = f.lovingItStickerFile;
    }

    @Override
    public boolean Execute(String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {

        // vote
        if (!testCommandWithArguments(CMD__VOTE, text))
            return false;

        if (senderId == null)
            return false;
        String voteFor = text.substring(text.indexOf(" ") + 1).trim();

        if (voteFor.length() > 70)
            return true;

        String realName = getBot().getRealName(voteFor);
        if (realName != null)
            voteFor = realName;

        synchronized (VOTES_LOCK) {
            try {
                FileUtils.writeStringToFile(currentVotesFile, String.format("(%d) %s\n", senderId, voteFor), Charset.defaultCharset(), true);
            } catch (Throwable t) {
                // log error
                logger.error("Failed to register vote", t);
                sendMessage(chatId, "Sorry, your vote wasn't counted.", senderId);
            }
        }

        return true;
    }

    private void scheduleNextAwardCeremony() {
        try {
            Date awardsDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Calendar c = Calendar.getInstance();
            c.setTime(awardsDate);
            c.add(Calendar.HOUR, 21);
            awardsDate = c.getTime();

            if (!votesDirectory.exists() && !votesDirectory.mkdir()) {
                throw new Exception("Unable to init awards ceremony");
            }
            final File awardResultFile = new File(votesDirectory, Settings.DF__FILE_NAME.format(awardsDate));

            // schedule to next day if already awarded
            if (awardResultFile.exists()) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            awardsDate = c.getTime();

            if (new Date().after(awardsDate)) {
                awardsDate = new Date();
            }

            // log
            logger.info("Scheduled awards ceremony to: " + Settings.DTF__TEXT.format(awardsDate));

            TimerTask awardsTask = new TimerTask() {
                @Override
                public void run() {

                    String winner = null;
                    // log
                    logger.info("Counting votes...");
                    try {
                        winner = runAwards(settings.getChatId(), awardResultFile);
                        // call the day
                        FileUtils.writeStringToFile(awardResultFile, winner, Charset.defaultCharset());
                        currentVotesFile.delete();
                    } catch (Exception t) {
                        // log error
                        logger.error("Awards failure", t);
                        try {
                            Thread.currentThread().sleep(10000);
                        } catch (InterruptedException t2) {
                        }
                    } finally {
                        // log
                        logger.info("Awarded: " + (winner == null ? "None" : "\"" + winner + "\""));
                        scheduleNextAwardCeremony();
                    }
                }

            };

            new Timer().schedule(awardsTask, awardsDate);
        } catch (Throwable t) {
            // log error
            logger.error("Awards scheduler failure", t);
        }
    }

    public String runAwards(String chatId, File awardResultFile) throws Exception {
        String winner = null;
        if (awardResultFile.exists()) {
            return null;
        }
        synchronized (VOTES_LOCK) {
            if (!currentVotesFile.exists()) {
                awardResultFile.createNewFile();
                return null;
            }

            // zero votes
            List<String> votes = FileUtils.readLines(currentVotesFile, Charset.defaultCharset());
            if (votes.isEmpty()) {
                awardResultFile.createNewFile();
                return null;
            }

            Map<String, Integer> counts = new HashMap<>();
            Map<String, String> userVotes = new HashMap<>();
            Pattern p = Pattern.compile("^\\(([^\\)]+)\\) (.+$)");
            for (String vote : votes) {
                Matcher m = p.matcher(vote);
                if (!m.matches())
                    continue;
                userVotes.put(m.group(1) + "-" + m.group(2), m.group(2));
            }

            for (String u : userVotes.values())
                counts.put(u, counts.containsKey(u) ? counts.get(u) + 1 : 1);

            // zero valid votes
            if (counts.isEmpty()) {
                awardResultFile.createNewFile();
                return null;
            }

            // announce winner
            winner = Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
            sendMessage(chatId, String.format(AWARDS_MESSAGE_FORMAT, winner, winner), null);

            // send sticker
            sendSticker(chatId, null, MembersHelper.getIconFile(settings, winner));
            return winner;
        }
    }

    @Override
    public String GetId() {
        return FEATURE_ID;
    }

}
