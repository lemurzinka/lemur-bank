package botBank.repo;

import botBank.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByCardNumber(String cardNumber);

    List<Card> findAllByUserId(long userId);

}
