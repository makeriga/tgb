package org.makeriga.tgbot;

import org.makeriga.tgbot.helpers.TgbHelper;

public class TgbMessage {

    private String chatId = null;
    private String text = null;
    private Integer senderId = null;
    private String senderTitle = null;
    private Integer messageId = null;
    private boolean isPrivateMessage = false;
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

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
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

    public boolean isPrivateMessage() {
        return isPrivateMessage;
    }

    public void setPrivateMessage(boolean isPrivateMessage) {
        this.isPrivateMessage = isPrivateMessage;
    }

    public TgbHelper.CallbackData getCallback() {
        return callback;
    }

    public void setCallback(TgbHelper.CallbackData callback) {
        this.callback = callback;
    }
}
