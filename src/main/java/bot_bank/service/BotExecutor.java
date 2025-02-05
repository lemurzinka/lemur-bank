package bot_bank.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * BotExecutor provides an interface for executing Telegram send message requests.
 * Implementations of this interface should handle the process of sending messages via the Telegram API.
 */

public interface BotExecutor {
    void execute(SendMessage message) throws TelegramApiException;
}
