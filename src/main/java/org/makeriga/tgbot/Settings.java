package org.makeriga.tgbot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Settings {

    /**
     * @return the botToken
     */
    public String getBotToken() {
        return botToken;
    }

    /**
     * @param botToken the botToken to set
     */
    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    /**
     * @return the homeDirectory
     */
    public String getHomeDirectory() {
        return homeDirectory;
    }

    /**
     * @param homeDirectory the homeDirectory to set
     */
    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    /**
     * @return the botUsername
     */
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * @param botUsername the botUsername to set
     */
    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    /**
     * @return the adminId
     */
    public Integer getAdminId() {
        return adminId;
    }

    /**
     * @param adminId the adminId to set
     */
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public static final DateFormat DTF__FILE_NAME = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
    public static final DateFormat DF__FILE_NAME = new SimpleDateFormat("dd-MM-yyyy");
    public static final DateFormat DTF__TEXT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final DateFormat TF__TEXT = new SimpleDateFormat("HH:mm");
    
    private String botToken = null;
    private String chatId = null;
    private String homeDirectory = null;
    private String botUsername = null;
    private Integer adminId = 1;

    private String usersMappings;

    /**
     * @return the chatId
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * @param chatId the chatId to set
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUsersMappings() {
        return usersMappings;
    }

    public void setUsersMappings(String usersMappings) {
        this.usersMappings = usersMappings;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "botToken='" + botToken + '\'' +
                ", chatId='" + chatId + '\'' +
                ", homeDirectory='" + homeDirectory + '\'' +
                ", botUsername='" + botUsername + '\'' +
                ", adminId=" + adminId +
                ", usersMappings='" + usersMappings + '\'' +
                '}';
    }
}
