package tests;

import bot_bank.bot.BotContext;
import bot_bank.bot.ChatBot;
import bot_bank.model.Account;
import bot_bank.model.Card;
import bot_bank.model.CardType;
import bot_bank.model.User;
import bot_bank.service.AccountService;
import bot_bank.service.CardAccountService;
import bot_bank.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CardAccountServiceTest contains unit tests for the CardAccountService class. It verifies the correctness
 * of methods related to creating and saving credit and debit card accounts and their interactions with
 * the Telegram bot context.
 */

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
        String accountNumber = "123456";
        String cardNumber = "654321";
        String cvv = "123";
        LocalDate expirationDate = LocalDate.now().plusYears(3);

        when(user.getId()).thenReturn(123L);
        when(accountService.generateAccountNumber()).thenReturn(accountNumber);
        when(cardService.generateCardNumber(CardType.CREDIT)).thenReturn(cardNumber);
        when(cardService.generateCVV()).thenReturn(cvv);
        when(cardService.generateExpirationDate()).thenReturn(expirationDate);

        cardAccountService.createCreditCardAndAccount(user, initialBalance, currency, context);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountService, times(1)).verifyAndSaveAccount(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardService, times(1)).addCard(cardCaptor.capture());


        verify(bot, times(1)).execute(any(SendMessage.class));

        assertEquals(initialBalance, capturedAccount.getCreditBalance());
        assertEquals(initialBalance, capturedAccount.getCurrentBalance());
        assertEquals(currency, capturedAccount.getCurrency());
    }

    @Test
    void testCreateDebitCardAndAccount() throws TelegramApiException {
        String currency = "USD";
        String accountNumber = "123456";
        String cardNumber = "654321";
        String cvv = "123";
        LocalDate expirationDate = LocalDate.now().plusYears(3);

        when(user.getId()).thenReturn(123L);
        when(accountService.generateAccountNumber()).thenReturn(accountNumber);
        when(cardService.generateCardNumber(CardType.DEBIT)).thenReturn(cardNumber);
        when(cardService.generateCVV()).thenReturn(cvv);
        when(cardService.generateExpirationDate()).thenReturn(expirationDate);

        cardAccountService.createDebitCardAndAccount(user, currency, context);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountService, times(1)).verifyAndSaveAccount(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardService, times(1)).addCard(cardCaptor.capture());


        verify(bot, times(1)).execute(any(SendMessage.class));

        assertEquals(BigDecimal.ZERO, capturedAccount.getCurrentBalance());
        assertEquals(currency, capturedAccount.getCurrency());
    }
}
