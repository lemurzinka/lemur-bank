package botBank.repo;

import botBank.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByCardNumber(String cardNumber);

    List<Card> findAllByUserId(long userId);

    Optional<Card> findByCardNumber(String cardNumber);

}
