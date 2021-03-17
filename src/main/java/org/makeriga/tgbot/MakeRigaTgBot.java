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
import org.makeriga.tgbot.helpers.TgbHelper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
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
        TgbMessage message;
        if (update.getMessage() != null) {
            message = extractMessage(update.getMessage().getText(), update.getMessage(), update.getMessage().getFrom(), null);
        }
        else if (update.getCallbackQuery() != null) {
            TgbHelper.CallbackData cbd = TgbHelper.decodeCallbackData(update.getCallbackQuery().getData());
            message = extractMessage(cbd.getData(), update.getCallbackQuery().getMessage(), update.getCallbackQuery().getFrom(), cbd);

            // answer query
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            try {
                answer.setShowAlert(false);
                answer.setCallbackQueryId(update.getCallbackQuery().getId());
                execute(answer);
            }
            catch (TelegramApiException t) {
                // log error
                logger.error("A", t);
            }            

            // delete keyboard markup
            EditMessageReplyMarkup del = new EditMessageReplyMarkup();
            try {
                del.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                del.setChatId(message.getChatId());
                del.setReplyMarkup(null);
                execute(del);
            }
            catch (TelegramApiException t) {
                // log error
                logger.error("C", t);
            }
        }
        else 
            return;
        
        if (message.getChatId() == null || message.getText() == null)
            return;
        
        // log request
        if (message.isPrivateMessage())
            logger.info(String.format("%s: %s", message.getSenderTitle() == null ? message.getSenderId().toString() : message.getSenderTitle(), message.getText()));
        
        // direct send
        if (message.getCallback() != null && message.getCallback().getFeatureId() != null && features.containsKey(message.getCallback().getFeatureId())) {
            if (execFeature(features.get(message.getCallback().getFeatureId()), message))
                return;
        }
        
        // global send
        for (Feature f : features.values()) {
            if (execFeature(f, message))
                break;
        }
    }
    
    private static boolean execFeature(Feature f, TgbMessage message) {
        try {
            if (f.Execute(message.getText(), message.isPrivateMessage(), message.getSenderId(), message.getSenderTitle(), message.getMessageId(), message.getChatId()))
                return true;
        }
        catch (Throwable t) {
            // log error
            logger.error(f.GetId() + " failure", t);
        }
        return false;
    }

    @Override
    public String getBotUsername() {
        return settings.getBotUsername();
    }
    
    public void SendPublicMessage(String text) {
        SendMessage(settings.getChatId(), text, null, null);
    }
    
    public void SendMessage(String chatId, String text, Integer replyTo, ReplyKeyboard replyMarkup) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setText(text);
        sendMessageRequest.setChatId(chatId);
        sendMessageRequest.setReplyToMessageId(replyTo);
        sendMessageRequest.setReplyMarkup(replyMarkup);

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
        SendMessage(chatId, text, replyTo, null);
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
    
    private TgbMessage extractMessage(String text, Message message, User user, TgbHelper.CallbackData cbd) {
        TgbMessage m = new TgbMessage();
        m.setCallback(cbd);
        m.setChatId(message.getChatId().toString());
        m.setText(text);
        m.setMessageId(message.getMessageId());
        m.setPrivateMessage("private".equals(message.getChat().getType()));

        if (user != null) {
            m.setSenderId(user.getId());
            m.setSenderTitle(user.getUserName());
            if (m.getSenderTitle() == null)
                m.setSenderTitle(user.getFirstName());
        }
        return m;
    }
}
