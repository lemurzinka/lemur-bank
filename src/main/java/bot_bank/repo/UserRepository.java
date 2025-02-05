package bot_bank.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import bot_bank.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByTelegramId(Long telegramId);

    User findByNumber(String Number);

}

