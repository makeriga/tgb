package org.makeriga.tgbot;

import org.makeriga.tgbot.helpers.TgbHelper;

public class TgbMessage {

    private String chatId = null;
    private String text = null;
    private Long senderId = null;
    private String senderTitle = null;
    private Integer messageId = null;
    private boolean privateMessage = false;
    private boolean bot = false;
    private TgbHelper.CallbackData callback = null;
    
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderTitle() {
        return senderTitle;
    }

    public void setSenderTitle(String senderTitle) {
        this.senderTitle = senderTitle;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public TgbHelper.CallbackData getCallback() {
        return callback;
    }

    public void setCallback(TgbHelper.CallbackData callback) {
        this.callback = callback;
    }
    
    public boolean isPrivateMessage() {
        return privateMessage;
    }
    
    public void setPrivateMessage(boolean privateMessage) {
        this.privateMessage = privateMessage;
    }
    
    public boolean isBot() {
        return bot;
    }
    
    public void setBot(boolean bot) {
        this.bot = bot;
    }

    @Override
    public String toString() {
        return "TgbMessage{" +
                "chatId='" + chatId + '\'' +
                ", text='" + text + '\'' +
                ", senderId=" + senderId +
                ", senderTitle='" + senderTitle + '\'' +
                ", messageId=" + messageId +
                ", privateMessage=" + privateMessage +
                ", bot=" + bot +
                ", callback=" + callback +
                '}';
    }
}
