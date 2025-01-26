package tests;

import botBank.bot.BotContext;
import botBank.bot.ChatBot;
import botBank.model.Account;
import botBank.model.Card;
import botBank.model.User;
import botBank.service.AccountService;
import botBank.service.CardAccountService;
import botBank.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardAccountServiceTest {

    @Mock
    private CardService cardService;

    @Mock
    private AccountService accountService;

    @Mock
    private BotContext context;

    @Mock
    private User user;

     @Mock
     private ChatBot bot;

    private CardAccountService cardAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardAccountService = new CardAccountService(cardService, accountService);
        when(context.getBot()).thenReturn(bot);
        when(context.getUser()).thenReturn(user);
    }

    @Test
    void testCreateCreditCardAndAccount() throws TelegramApiException {
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        String currency = "USD";
        Card card = new Card();
        Account account = new Account();
        when(user.getId()).thenReturn(123L);
        when(cardService.createCard("CREDIT")).thenReturn(card);
        when(accountService.createAccount(user)).thenReturn(account);

        cardAccountService.createCreditCardAndAccount(user, initialBalance, currency, context);

        verify(accountService, times(1)).verifyAndSaveAccount(account);
        verify(cardService, times(1)).addCard(card);
        verify(bot, times(1)).execute(any(SendMessage.class));
        assertEquals(initialBalance, account.getCreditBalance());
        assertEquals(initialBalance, account.getCurrentBalance());
        assertEquals(currency, account.getCurrency());
    }

    @Test
    void testCreateDebitCardAndAccount() throws TelegramApiException {
        String currency = "USD";
        Card card = new Card();
        Account account = new Account();
        when(user.getId()).thenReturn(123L);
        when(cardService.createCard("DEBIT")).thenReturn(card);
        when(accountService.createAccount(user)).thenReturn(account);

        cardAccountService.createDebitCardAndAccount(user, currency, context);

        verify(accountService, times(1)).verifyAndSaveAccount(account);
        verify(cardService, times(1)).addCard(card);
        verify(bot, times(1)).execute(any(SendMessage.class));
        assertEquals(BigDecimal.ZERO, account.getCurrentBalance());
        assertEquals(currency, account.getCurrency());
    }
}
