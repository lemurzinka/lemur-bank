package botBank.service;

import botBank.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import botBank.repo.UserRepository;


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
}

