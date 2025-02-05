package bot_bank.service;

import bot_bank.bot.BotContext;
import bot_bank.model.User;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


/**
 * UserListingService provides methods for listing users and sending messages to users via Telegram.
 */
@Service
@AllArgsConstructor
public class UserListingService {

    private static final Logger LOGGER = LogManager.getLogger(UserListingService.class);

    private final UserService userService;

    public void listUsers(BotContext context) {
        StringBuilder sb = new StringBuilder("All users list:\r\n");
        List<User> users = userService.findAllUsers();

        users.forEach(user -> sb.append(user.getTelegramId())
                .append(' ')
                .append(user.getNumber())
                .append(' ')
                .append(user.getEmail())
                .append("\r\n"));

        LOGGER.info("Listing all users");
        sendMessage(context, sb.toString());
    }

    public static void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);

        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send message to chat ID: {}", context.getUser().getTelegramId(), e);
        }
    }
}
