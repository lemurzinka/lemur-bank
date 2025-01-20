package botBank.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotExecutor {
    void execute(SendMessage message) throws TelegramApiException;
}
