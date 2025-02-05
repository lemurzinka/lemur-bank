package bot_bank.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * MessageService provides methods for sending messages via the Telegram bot.
 * It uses the BotExecutor to execute SendMessage requests, handling any exceptions that occur.
 */

@Service
public class MessageService {

    private final BotExecutor botExecutor;

    public MessageService(BotExecutor botExecutor) {
        this.botExecutor = botExecutor;
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(chatId));
        message.setText(text);
        try {
            botExecutor.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
