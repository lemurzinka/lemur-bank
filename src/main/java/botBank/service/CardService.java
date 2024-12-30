package botBank.service;

import botBank.model.Card;
import botBank.model.CardType;
import org.springframework.stereotype.Service;
import botBank.repo.CardRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;


@Service
public class CardService {

    private static final Random RANDOM = new Random();



    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public void addCard(Card card) {
        cardRepository.save(card);
    }




public String generateCardNumber() {
StringBuilder cardNumber = new StringBuilder();
for (int i = 0; i < 16; i++) {
    cardNumber.append(RANDOM.nextInt(10));
}
return cardNumber.toString();
}

public LocalDate generateExpirationDate() {
    LocalDate today = LocalDate.now();
    LocalDate expirationDate = today.plusYears(3);
    return expirationDate;
}

public String generateCVV() {
        int cvv = RANDOM.nextInt(900) + 100;
        return String.valueOf(cvv);
}

public Card createCard(String type){
        Card card = new Card();
        card.setCardNumber(generateCardNumber());
        card.setExpirationDate(generateExpirationDate());
        card.setCvv(generateCVV());
        CardType enumType = CardType.valueOf(type);
        card.setCardType(enumType);

        return card;
}

}
