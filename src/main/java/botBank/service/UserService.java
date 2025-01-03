package botBank.service;

import botBank.bot.BotContext;
import botBank.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import botBank.repo.UserRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;



@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public User findByNumber(String Number) {
        return userRepository.findByNumber(Number);
    }

    @Transactional
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Transactional()
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
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

        sendMessage(context, sb.toString());
    }

    public static void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);

        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}

