package org.makeriga.tgbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.makeriga.tgbot.features.Feature;
import org.makeriga.tgbot.helpers.FeaturesHelper;
import org.makeriga.tgbot.helpers.MembersHelper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MakeRigaTgBot extends TelegramLongPollingBot {
    
    private static final Logger logger = Logger.getLogger(MakeRigaTgBot.class);
    
    private Settings settings = null;
    private final Map<String, Feature> features = new HashMap<>();
    private final Map<String, String> doorToMemberMappings = new HashMap<>();
    private final Map<String, String> alternativeNames = new HashMap<>();
    private final Map<String, Date> requestRates = new ConcurrentHashMap<>();
    
    public MakeRigaTgBot(Settings settings) throws Throwable {
        this.settings = settings;

        // parse members
        MembersHelper.ParseMembers(doorToMemberMappings, alternativeNames);
        
        // load features
        FeaturesHelper.LoadFeatures(this, settings, features);
    }
    
    @Override
    public String getBotToken() {
        return settings.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatId;
        String text;
        Integer senderId = null;
        String senderTitle = null;
        Integer messageId;
        boolean isPrivateMessage;
        if (update.getMessage() != null) {
            chatId = update.getMessage().getChatId().toString();
            text = update.getMessage().getText();
            messageId = update.getMessage().getMessageId();
            isPrivateMessage = "private".equals(update.getMessage().getChat().getType());

            if (update.getMessage().getFrom() != null) {
                senderId = update.getMessage().getFrom().getId();
                senderTitle = update.getMessage().getFrom().getUserName();
                if (senderTitle == null)
                    senderTitle = update.getMessage().getFrom().getFirstName();
            }
        }
//        else if (update.getChannelPost()!= null) {
//            chatId = update.getChannelPost().getChatId().toString();
//            text = update.getChannelPost().getText();
//            replyTo = update.getChannelPost().getMessageId();
//        }
        else 
            return;
        
        if (chatId == null || text == null)
            return;
        
        // log request
        if (isPrivateMessage)
            logger.info(String.format("%s: %s", senderTitle == null ? senderId.toString() : senderTitle, text));
        
        for (Feature f : features.values()) {
            try {
                if (f.Execute(text, isPrivateMessage, senderId, senderTitle, messageId, chatId))
                    break;
            }
            catch (Throwable t) {
                // log error
                logger.error(f.GetId() + " failure", t);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return settings.getBotUsername();
    }
    
    public void SendPublicMessage(String text) {
        SendMessage(settings.getChatId(), text, null);
    }
    
    public void SendMessage(String chatId, String text, Integer replyTo) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setText(text);
        sendMessageRequest.setChatId(chatId);
        sendMessageRequest.setReplyToMessageId(replyTo);

        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException ex) {
            // log
            logger.error("Unable to send message", ex);
        }
    }    
    
    public void SendAntispamMessage(String chatId, String text, Integer replyTo, String antispamMessagePreffix, Integer senderUserId) {
        if (!settings.getAdminId().equals(senderUserId) && (antispamMessagePreffix == null || !TestRequestRate(antispamMessagePreffix + "-as-" + chatId)))
            return;
        SendMessage(chatId, text, replyTo);
    }
    
    public void SendPhoto(String chatId, Integer replyTo, InputStream is, String fileName) throws TelegramApiException {
        SendPhoto p = new SendPhoto();  
        p.setPhoto(new InputFile(is, fileName));
        p.setChatId(chatId);
        p.setReplyToMessageId(replyTo);
        execute(p);
    }
    
    public void SendSticker(String chatId, Integer replyTo, File stickerFile) {
        if (stickerFile == null || !stickerFile.exists())
            return;
        SendSticker sendStickerRequest = new SendSticker();
        InputStream is = null;
        sendStickerRequest.setChatId(chatId);
        sendStickerRequest.setReplyToMessageId(replyTo);

        try {
            sendStickerRequest.setSticker(new InputFile(is = new FileInputStream(stickerFile), stickerFile.getName()));
            execute(sendStickerRequest);
        } catch (Throwable ex) {
            // log
            logger.error("Unable to send message", ex);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }
    
    public boolean TestRequestRate(String requestKey) {
        synchronized (requestRates) {
            Date latestTime = requestRates.get(requestKey);
            if (latestTime != null && new Date().getTime() - latestTime.getTime() < 60000 )
                return false;
            requestRates.put(requestKey, new Date());
            return true;
        }
    }
    
    public void RemoveRequestRate(String key) {
        if (key == null)
            return;
        if (!requestRates.containsKey(key))
            return;
        requestRates.remove(key);
    }

    public Map<String, String> getDoorToMembermappings() {
        return doorToMemberMappings;
    }
    
    public String getRealName(String alternativeName) {
        if (alternativeName == null)
            return null;
        return alternativeNames.get(alternativeName.toLowerCase());
    }
    
    public Map<String, Feature> getFeatures() {
        return features;
    }
}
