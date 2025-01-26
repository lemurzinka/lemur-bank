package botBank.service;

import botBank.bot.BotContext;
import botBank.model.Card;
import botBank.model.CardType;
import botBank.repo.CardRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static botBank.service.UserService.sendMessage;

@Service
public class CardService {

    private static final Logger LOGGER = LogManager.getLogger(CardService.class);
    private static final Random RANDOM = new Random();

    @PersistenceContext
    private EntityManager entityManager;

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional(readOnly = true)
    public Card findCardByCardNumber(String cardNumber) {
        LOGGER.info("Finding card by card number: {}", cardNumber);
        return cardRepository.findByCardNumber(cardNumber).orElse(null);
    }
    @Transactional(readOnly = true)
    public Optional<Card> findByCardNumber(String cardNumber) {
        LOGGER.info("Finding card by card number: {}", cardNumber);
        return cardRepository.findByCardNumber(cardNumber);
    }

    @Transactional(readOnly = true)
    public Optional<Card> findById(Long id) {
        LOGGER.info("Finding card by id: {}", id);
        return cardRepository.findById(id);
    }

    @Transactional
    public void addCard(Card card) {
        LOGGER.info("Adding card: {}", card.getCardNumber());
        while (cardRepository.existsByCardNumber(card.getCardNumber())) {
            card.setCardNumber(generateCardNumber(card.getCardType())); // Generating new number if duplicate
            LOGGER.warn("Duplicate card number found, generating new number: {}", card.getCardNumber());
        }
        cardRepository.save(card);
        LOGGER.info("Card saved: {}", card.getCardNumber());
    }

    public String generateCardNumber(CardType cardType) {
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
        return generateCardNumber(bin);
    }

    private String generateCardNumber(String bin) {
        StringBuilder cardNumber = new StringBuilder(bin);

        for (int i = bin.length(); i < 15; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

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
        LOGGER.info("Generated expiration date: {}", expirationDate);
        return expirationDate;
    }

    public String generateCVV() {
        int cvv = RANDOM.nextInt(900) + 100;
        LOGGER.info("Generated CVV: {}", cvv);
        return String.valueOf(cvv);
    }

    public Card createCard(String type) {
        Card card = new Card();
        card.setExpirationDate(generateExpirationDate());
        card.setCvv(generateCVV());
        CardType enumType = CardType.valueOf(type);
        card.setCardNumber(generateCardNumber(enumType));
        card.setCardType(enumType);
        LOGGER.info("Created card: {}", card.getCardNumber());
        return card;
    }

    @Transactional(readOnly = true)
    public List<Card> getCardsByUserId(long userId) {
        LOGGER.info("Getting cards by user ID: {}", userId);
        List<Card> cards = cardRepository.findAllByUserId(userId);
        if (cards.isEmpty()) {
            LOGGER.info("No cards found for user ID: {}", userId);
        }
        return cards;
    }

    public String formatCardDetails(List<Card> cards) {
        StringBuffer response = new StringBuffer("Your cards:\n");
        for (Card card : cards) {
            response.append("Card Number: ").append(card.getCardNumber())
                    .append("\nExpiry Date: ").append(card.getExpirationDate())
                    .append("\nCVV: ").append(card.getCvv())
                    .append("\nCard Type: ").append(card.getCardType().toString().toLowerCase());

            BigDecimal currentBalance = card.getAccount().getCurrentBalance();
            response.append("\nBalance: ").append(currentBalance);

            if (card.getCardType() == CardType.CREDIT) {
                BigDecimal creditBalance = card.getAccount().getCreditBalance();
                BigDecimal debt = creditBalance.subtract(currentBalance);
                if (debt.compareTo(BigDecimal.ZERO) > 0) {
                    response.append(" (Debt: ").append(debt).append(" UAH)");
                }
            }

            response.append("\n-------------------------------------------------------------\n");
        }
        LOGGER.info("Formatted card details for user");
        return response.toString();
    }



    @Transactional(readOnly = true)
    public List<Card> findAllCards() {
        LOGGER.info("Finding all cards");
        return cardRepository.findAll();
    }

    public void listCards(BotContext context) {
        StringBuffer sb = new StringBuffer("All cards list:\r\n");
        List<Card> cards = findAllCards();

        if (cards.isEmpty()) {
            sb.append("No cards found.");
        } else {
            cards.forEach(card -> sb.append(card.getCardNumber())
                    .append(" ")
                    .append(card.getExpirationDate())
                    .append(" ")
                    .append(card.getCardType())
                    .append("\r\n"));
        }

        LOGGER.info("Listing all cards");
        sendMessage(context, sb.toString());
    }


    @Transactional
    public void updateCard(Card card) {
        LOGGER.info("Updating card (before save): {}", card);
        cardRepository.save(card);
        entityManager.flush(); // Ensure changes are persisted immediately
        LOGGER.info("Updating card (after flush): {}", card);


        Optional<Card> updatedCardOpt = cardRepository.findById(card.getId());
        if (updatedCardOpt.isPresent()) {
            Card updatedCard = updatedCardOpt.get();
            LOGGER.info("Updated card from DB: {}", updatedCard);
        } else {
            LOGGER.warn("Updated card not found in DB: id={}", card.getId());
        }
    }
}
