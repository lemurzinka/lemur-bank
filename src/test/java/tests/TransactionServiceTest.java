package tests;

import botBank.bot.BotContext;
import botBank.bot.ChatBot;
import botBank.model.Account;
import botBank.model.Card;
import botBank.model.Transaction;
import botBank.model.TransactionDetail;
import botBank.model.User;
import botBank.repo.TransactionRepository;
import botBank.service.AccountService;
import botBank.service.CardService;
import botBank.service.CurrencyRateService;
import botBank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TransactionServiceTest contains unit tests for the TransactionService class. It verifies the correctness
 * of methods related to handling transactions, such as saving transactions, parsing amounts, validating sender
 * details, converting currencies, processing transactions, and sending messages via the Telegram bot context.
 */

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyRateService currencyRateService;

    @Mock
    private AccountService accountService;

    @Mock
    private CardService cardService;

    @Mock
    private BotContext context;

    @Mock
    private User user;

    @Mock
    private TransactionDetail transactionDetail;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private ChatBot bot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getUser()).thenReturn(user);
        when(user.getTransactionDetail()).thenReturn(transactionDetail);
        when(context.getBot()).thenReturn(bot);
    }

    @Test
    void testSaveTransaction() {
        Transaction transaction = new Transaction();

        transactionService.saveTransaction(transaction);

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void testParseAmount() {
        String amountStr = "100.50";
        when(transactionDetail.getAmount()).thenReturn(new BigDecimal(amountStr));

        BigDecimal result = transactionService.parseAmount(amountStr, context);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result);
    }

    @Test
    void testParseInvalidAmount() {
        String amountStr = "invalid";

        BigDecimal result = transactionService.parseAmount(amountStr, context);

        assertNull(result);
    }

    @Test
    void testValidateSenderDetails() {
        BigDecimal senderAmount = new BigDecimal("100.50");
        Card card = new Card();
        card.setCvv("123");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        Account senderAccount = new Account();
        senderAccount.setCurrentBalance(new BigDecimal("200.00"));
        card.setAccount(senderAccount);

        when(cardService.findByCardNumber(anyString())).thenReturn(Optional.of(card));
        when(transactionDetail.getSenderCardNumber()).thenReturn("1111222233334444");
        when(transactionDetail.getSenderCvv()).thenReturn("123");
        when(transactionDetail.getSenderExpDate()).thenReturn(LocalDate.now().plusYears(1).toString());

        boolean result = transactionService.validateSenderDetails(context, senderAmount);

        assertTrue(result);
    }

    @Test
    void testValidateInvalidSenderDetails() {
        BigDecimal senderAmount = new BigDecimal("100.50");

        when(cardService.findByCardNumber(anyString())).thenReturn(Optional.empty());
        when(transactionDetail.getSenderCardNumber()).thenReturn("1111222233334444");
        when(transactionDetail.getSenderCvv()).thenReturn("123");
        when(transactionDetail.getSenderExpDate()).thenReturn(LocalDate.now().plusYears(1).toString());

        boolean result = transactionService.validateSenderDetails(context, senderAmount);

        assertFalse(result);
    }

    @Test
    void testGetRecipientAccount() {
        Card card = new Card();
        card.setAccount(new Account());

        when(cardService.findByCardNumber(anyString())).thenReturn(Optional.of(card));
        when(transactionDetail.getRecipientCardNumber()).thenReturn("5555666677778888");

        Account result = transactionService.getRecipientAccount(context);

        assertNotNull(result);
    }

    @Test
    void testConvertCurrencyIfNeeded() {
        Account senderAccount = new Account();
        senderAccount.setCurrency("USD");
        Account recipientAccount = new Account();
        recipientAccount.setCurrency("EUR");
        BigDecimal senderAmount = new BigDecimal("100.50");

        when(currencyRateService.getRate("USD", "EUR")).thenReturn(0.85);

        BigDecimal result = transactionService.convertCurrencyIfNeeded(senderAccount, recipientAccount, senderAmount, context);

        assertNotNull(result);
        assertEquals(new BigDecimal("85.4250"), result);
    }

    @Test
    void testConvertCurrencyIfNeededWithNoConversion() {
        Account senderAccount = new Account();
        senderAccount.setCurrency("USD");
        Account recipientAccount = new Account();
        recipientAccount.setCurrency("USD");
        BigDecimal senderAmount = new BigDecimal("100.50");

        BigDecimal result = transactionService.convertCurrencyIfNeeded(senderAccount, recipientAccount, senderAmount, context);

        assertNotNull(result);
        assertEquals(senderAmount, result);
    }

    @Test
    void testProcessTransaction() {
        Account senderAccount = new Account();
        senderAccount.setCurrentBalance(new BigDecimal("500.00"));
        Account recipientAccount = new Account();
        recipientAccount.setCurrentBalance(new BigDecimal("200.00"));
        BigDecimal senderAmount = new BigDecimal("100.50");
        BigDecimal recipientAmount = new BigDecimal("85.4250");

        transactionService.processTransaction(senderAccount, recipientAccount, senderAmount, recipientAmount, context);

        verify(accountService, times(1)).saveAccount(senderAccount);
        verify(accountService, times(1)).saveAccount(recipientAccount);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testSendMessage() throws TelegramApiException {
        long chatId = 123L;
        String text = "Test message";
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(chatId));
        message.setText(text);

        TransactionService.sendMessage(context, text);

        verify(context.getBot(), times(1)).execute(any(SendMessage.class));
    }
}
