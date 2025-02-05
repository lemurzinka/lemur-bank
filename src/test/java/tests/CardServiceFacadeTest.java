package tests;

import bot_bank.bot.BotContext;
import bot_bank.bot.ChatBot;
import bot_bank.model.Card;
import bot_bank.model.CardType;
import bot_bank.model.User;
import bot_bank.service.CardService;
import bot_bank.service.CardServiceFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardServiceFacadeTest {

    @Mock
    private CardService cardService;

    @Mock
    private BotContext botContext;

    @Mock
    private ChatBot bot;

    @Mock
    private User user;

    @InjectMocks
    private CardServiceFacade cardServiceFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(botContext.getUser()).thenReturn(user);
        when(botContext.getBot()).thenReturn(bot);
        when(user.getTelegramId()).thenReturn(123456789L);
    }

    @Test
    void testListCards_WithCards() throws TelegramApiException {
        Card card1 = new Card();
        card1.setCardNumber("12345");
        card1.setExpirationDate(LocalDate.of(2024, 12, 1));
        card1.setCardType(CardType.CREDIT);

        Card card2 = new Card();
        card2.setCardNumber("67890");
        card2.setExpirationDate(LocalDate.of(2023, 11, 1));
        card2.setCardType(CardType.DEBIT);

        List<Card> cards = Arrays.asList(card1, card2);
        when(cardService.findAllCards()).thenReturn(cards);

        cardServiceFacade.listCards(botContext);

        verify(cardService, times(1)).findAllCards();
        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testListCards_NoCards() throws TelegramApiException {
        List<Card> cards = Collections.emptyList();
        when(cardService.findAllCards()).thenReturn(cards);

        cardServiceFacade.listCards(botContext);

        verify(cardService, times(1)).findAllCards();
        verify(bot, times(1)).execute(any(SendMessage.class));
    }
}
