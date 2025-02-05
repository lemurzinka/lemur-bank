package tests;

import bot_bank.model.Card;
import bot_bank.model.CardType;
import bot_bank.repo.CardRepository;
import bot_bank.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CardServiceTest contains unit tests for the CardService class. It verifies the correctness
 * of methods related to card operations such as finding, adding, generating, creating,
 * updating cards, and interacting with the EntityManager.
 */

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EntityManager entityManager;

    private CardService cardService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        cardService = new CardService(cardRepository);

        // reflection for EM
        Field entityManagerField = CardService.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        entityManagerField.set(cardService, entityManager);
    }

    @Test
    void testFindCardByCardNumber() {
        String cardNumber = "4532771234567890";
        Card card = new Card();
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));

        Card foundCard = cardService.findCardByCardNumber(cardNumber);

        assertNotNull(foundCard);
        verify(cardRepository, times(1)).findByCardNumber(cardNumber);
    }

    @Test
    void testAddCard() {
        Card card = new Card();
        card.setCardNumber("4532771234567890");
        when(cardRepository.existsByCardNumber(card.getCardNumber())).thenReturn(false);

        cardService.addCard(card);

        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testGenerateCardNumber() {
        String cardNumber = cardService.generateCardNumber(CardType.CREDIT);

        assertNotNull(cardNumber);
        assertTrue(cardNumber.startsWith("453277"));
    }

    @Test
    void testCreateCard() {
        String type = "CREDIT";
        Card card = cardService.createCard(type);

        assertNotNull(card);
        assertEquals(CardType.CREDIT, card.getCardType());
        assertNotNull(card.getCardNumber());
        assertNotNull(card.getExpirationDate());
        assertNotNull(card.getCvv());
    }

    @Test
    void testGetCardsByUserId() {
        long userId = 123L;
        List<Card> cards = Arrays.asList(new Card(), new Card());
        when(cardRepository.findAllByUserId(userId)).thenReturn(cards);

        List<Card> foundCards = cardService.getCardsByUserId(userId);

        assertNotNull(foundCards);
        assertEquals(2, foundCards.size());
        verify(cardRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void testUpdateCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("4532771234567890");
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        cardService.updateCard(card);

        verify(cardRepository, times(1)).save(card);
        verify(entityManager, times(1)).flush();
        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void testGenerateExpirationDate() {
        LocalDate expirationDate = cardService.generateExpirationDate();

        assertNotNull(expirationDate);
        assertTrue(expirationDate.isAfter(LocalDate.now()));
    }

    @Test
    void testGenerateCVV() {
        String cvv = cardService.generateCVV();

        assertNotNull(cvv);
        assertEquals(3, cvv.length());
        assertTrue(cvv.matches("\\d{3}"));
    }
}
