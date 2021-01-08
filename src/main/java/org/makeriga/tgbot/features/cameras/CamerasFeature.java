package org.makeriga.tgbot.features.cameras;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CamerasFeature extends Feature {
    
    public static final String FEATURE_ID = "cameras";

    private static final Logger logger = Logger.getLogger(CamerasFeature.class);
    
    private static final String CMD__PRINTERCAM = "/printercam";
    
    public CamerasFeature(MakeRigaTgBot bot, Settings settings) {
        super(bot, settings);
    }
    
    @Override
    public void Init() {
        super.Init();
        
        // commands descriptions
        AddPublicCommandDescription(CMD__PRINTERCAM, "3d printer state");
    }

    @Override
        public boolean Execute(String text, boolean isPrivateMessage, Integer senderId, String senderTitle, Integer messageId, String chatId) {
        
        // send an image from 3d printers camera
        if (testCommandWithoutArguments(CMD__PRINTERCAM, text)) {
            String requestKey = chatId + "-" + CMD__PRINTERCAM;
            if (!getBot().TestRequestRate(requestKey)) {
                sendAntispamMessage(chatId, "Try again in a minute", !isPrivateMessage ? messageId : null, CMD__PRINTERCAM, senderId);
                return true;
            }

            // send
            sendPic(chatId, !isPrivateMessage ? messageId : null, requestKey, "./3dprinter.sh", String.format("riharda-printeris-%s.jpg", Settings.DTF__FILE_NAME.format(new Date())));

            return true;
        }
        return false;
    }
    
    private void sendPic(String chatId, Integer replyTo, String requestKey, String execName, String fileName) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(execName);
            builder.directory(new File(settings.getHomeDirectory(), "features/cameras"));
            Process process = builder.start();
            int result = process.waitFor();
            if (result != 0)
                throw new Exception("Exit code: " + result);

            InputStream is = null;
            try {
                sendPhoto(chatId, replyTo, is = process.getInputStream(), fileName);
            }
            catch (TelegramApiException e) {
                logger.error("TG api error", e);
                getBot().RemoveRequestRate(requestKey);
                sendMessage(chatId, "Nevarēja aizsūtīt bildi, jo jāskatās logfaili.", replyTo);
            }
            finally {
                IOUtils.closeQuietly(is);
            }
        } catch (Throwable t) {
            logger.error("Unable to snap", t);
            getBot().RemoveRequestRate(requestKey);
            sendMessage(chatId, "Failed to access a camera :(", replyTo);
        }
    }
    
    @Override
    public String GetId() {
        return FEATURE_ID;
    }
    
}
