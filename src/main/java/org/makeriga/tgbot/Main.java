package org.makeriga.tgbot;


import org.makeriga.tgbot.helpers.MembersHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        Settings settings = new Settings();
        settings.setBotToken(System.getenv("bot_token"));
        settings.setBotUsername(System.getenv("bot_username"));
        settings.setChatId(System.getenv("chat_id"));
        settings.setHomeDirectory(System.getenv("home_dir"));
        try {
            settings.setAdminId(Integer.valueOf(System.getenv("admin_id")));
        } catch (NumberFormatException e) {
            logger.error("Could not set adminid", e);
        }
        settings.setUsersMappings(System.getenv("users_mappings"));

        try {
            assert settings.getBotToken() != null && settings.getBotUsername() != null && settings.getChatId() != null && settings.getHomeDirectory() != null;
            // init helper
            MembersHelper.init(settings);
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new MakeRigaTgBot(settings));

            logger.info("Bot started");

        } catch (Throwable t) {
            // log error
            logger.error("Unable to start", t);
        }
    }
}
