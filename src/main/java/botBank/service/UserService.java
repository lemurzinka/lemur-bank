package botBank.service;

import botBank.bot.BotContext;
import botBank.model.User;
import botBank.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * UserService provides methods for managing user operations such as finding, adding,
 * updating, and listing users. It interacts with the UserRepository to perform database
 * operations and communicates with users via Telegram messages.
 */

@Service
@AllArgsConstructor
public class UserService {

    private static final Logger LOGGER = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;



    @Transactional(readOnly = true)
    public User findByTelegramId(Long telegramId) {
        LOGGER.info("Finding user by Telegram ID: {}", telegramId);
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public User findByNumber(String number) {
        LOGGER.info("Finding user by number: {}", number);
        return userRepository.findByNumber(number);
    }

    @Transactional
    public void addUser(User user) {
        LOGGER.info("Adding new user: {}", user);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(User user) {
        LOGGER.info("Updating user: {}", user);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        LOGGER.info("Finding all users");
        return userRepository.findAll();
    }

    public void listUsers(BotContext context) {
        StringBuilder sb = new StringBuilder("All users list:\r\n");
        List<User> users = findAllUsers();

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
