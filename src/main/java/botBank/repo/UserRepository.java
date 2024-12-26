package botBank.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import botBank.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByTelegramId(Long telegramId);

    User findByNumber(String Number);

}

