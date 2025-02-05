package bot_bank.service;


import bot_bank.model.User;
import bot_bank.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
}
