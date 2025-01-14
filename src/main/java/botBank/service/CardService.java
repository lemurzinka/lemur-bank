package botBank.service;


import botBank.model.Card;
import botBank.model.CardType;
import org.springframework.stereotype.Service;
import botBank.repo.CardRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;


@Service
public class CardService {

    private static final Random RANDOM = new Random();



    private final CardRepository cardRepository;


    @Transactional
    public Optional<Card> findByCardNumber (String cardNumber){
     return cardRepository.findByCardNumber(cardNumber);
    }

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;


    }

    @Transactional
    public void addCard(Card card) {
        while (cardRepository.existsByCardNumber(card.getCardNumber())) {
            card.setCardNumber(generateCardNumber(card.getCardType())); // Generating of new number if duplicate
        }

        cardRepository.save(card);
    }





    public String generateCardNumber(CardType cardType) {
        // BIN for Visa types
        String bin;
        switch (cardType) {
            case CREDIT:
                bin = "453277"; // BIN for credit Visa
                break;
            case DEBIT:
                bin = "453279"; // BIN for debit Visa
                break;
            default:
                throw new IllegalArgumentException("Unsupported card type: " + cardType);
        }

        // Generating number for BIN and Luhn (algoritm)
        return generateCardNumber(bin);
    }

    private String generateCardNumber(String bin) {
        StringBuilder cardNumber = new StringBuilder(bin);

        // Random digit by 15 pos
        for (int i = bin.length(); i < 15; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        // Control digit with Luhn
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        // From right to left
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10; // control digit
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
        card.setExpirationDate(generateExpirationDate());
        card.setCvv(generateCVV());
        CardType enumType = CardType.valueOf(type);
        card.setCardNumber(generateCardNumber(enumType));
        card.setCardType(enumType);

        return card;
    }


    @Transactional
    public List<Card> getCardsByUserId(long userId) {
        return cardRepository.findAllByUserId(userId);
    }
    public String formatCardDetails(List<Card> cards) {
        StringBuilder response = new StringBuilder("Your cards:\n");
        for (Card card : cards) {
            response.append("Card Number: ").append(card.getCardNumber())
                    .append("\nExpiry Date: ").append(card.getExpirationDate())
                    .append("\nCVV: ").append(card.getCvv())
                    .append("\nCard Type: ").append(card.getCardType().toString().toLowerCase())
                    .append("\nBalance: "+card.getAccount().getCurrentBalance())
                    .append("\n-------------------------------------------------------------\n");
        }
        return response.toString();
    }

}