package tests;

import botBank.bot.BotContext;
import botBank.bot.BotState;
import botBank.model.*;
import botBank.service.AccountService;
import botBank.service.CardAccountService;
import botBank.service.CardService;
import botBank.service.CommandService;
import botBank.service.CurrencyRateService;
import botBank.service.MessageService;
import botBank.service.TransactionService;
import botBank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import botBank.bot.ChatBot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CardService cardService;

    @Mock
    private CardAccountService cardAccountService;

    @Mock
    private CurrencyRateService rateService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountService accountService;

    @Mock
    private MessageService messageService;

    @Mock
    private BotContext context;

    @Mock
    private User user;

    @Mock
    private  ChatBot bot;

    private CommandService commandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandService = new CommandService(userService, cardService, cardAccountService, rateService, transactionService, accountService, messageService);
        when(context.getUser()).thenReturn(user);
        when(context.getCardAccountService()).thenReturn(cardAccountService);
        when(context.getBot()).thenReturn(bot);
    }

    @Test
    void testHandleCommandUpdate() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/update", context);

        verify(user).setStateId(BotState.EnterPasswordForUpdate.ordinal());
        verify(messageService).sendMessage(123L, "You selected to update.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandAddCard() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/addcard", context);

        verify(user).setStateId(BotState.AddCard.ordinal());
        verify(messageService).sendMessage(123L, "You selected to add a card.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandListUsers() {
        commandService.handleCommand("/listusers", context);

        verify(userService).listUsers(context);
    }

    @Test
    void testHandleCommandBanUser() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/banuser", context);

        verify(user).setStateId(BotState.BanUser.ordinal());
        verify(messageService).sendMessage(123L, "You selected to ban user.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandUnbanUser() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/unbanuser", context);

        verify(user).setStateId(BotState.UnbanUser.ordinal());
        verify(messageService).sendMessage(123L, "You selected to unban user.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandCredit() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("credit", context);

        verify(cardAccountService).createCreditCardAndAccount(eq(user), eq(BigDecimal.valueOf(5000)), eq("UAH"), eq(context));
        verify(user).setStateId(BotState.Menu.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandDebit() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("debit", context);

        verify(messageService).sendMessage(123L, "You selected to add a debit card.");
        verify(user).setStateId(BotState.ChoseCurrency.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandCurrency() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("USD", context);

        verify(cardAccountService).createDebitCardAndAccount(eq(user), eq("USD"), eq(context));
        verify(user).setStateId(BotState.Menu.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandMyCards_NoCards() {

        when(user.getTelegramId()).thenReturn(123L);
        when(user.getId()).thenReturn(1L);
        when(cardService.getCardsByUserId(1L)).thenReturn(List.of());

        // Викликаємо метод handleCommand
        commandService.handleCommand("/mycards", context);


        verify(messageService).sendMessage(123L, "No cards found for user ID: 1");
    }

    @Test
    void testHandleCommandMyCards_WithCards() {

        Card card = new Card();
        card.setCardNumber("1234567890123456");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setCvv("123");
        card.setCardType(CardType.DEBIT);
        Account account = new Account();
        account.setCurrentBalance(new BigDecimal("1000.00"));
        card.setAccount(account);

        when(user.getTelegramId()).thenReturn(123L);
        when(user.getId()).thenReturn(1L);
        when(cardService.getCardsByUserId(1L)).thenReturn(List.of(card));
        when(cardService.formatCardDetails(anyList())).thenReturn("Formatted card details");


        commandService.handleCommand("/mycards", context);


        verify(messageService).sendMessage(123L, "Formatted card details");
    }


    @Test
    void testHandleCommandRates() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/rates", context);

        verify(messageService).sendMessage(123L, rateService.getFormattedRates());
    }

    @Test
    void testHandleCommandSend() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/send", context);

        verify(user).setTransactionDetail(any(TransactionDetail.class));
        verify(user).setStateId(BotState.EnterCardNumberForTransaction.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandHi() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/hi", context);

        verify(user).setStateId(BotState.EnterPhone.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandListCards() {
        commandService.handleCommand("/listcards", context);

        verify(cardService).listCards(context);
    }

    @Test
    void testHandleCommandListAccounts() {
        commandService.handleCommand("/listaccounts", context);

        verify(accountService).listAccounts(context);
    }

    @Test
    void testHandleCommandBanCard() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/bancard", context);

        verify(user).setStateId(BotState.BanCard.ordinal());
        verify(messageService).sendMessage(123L, "You selected to ban card.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandUnbanCard() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/unbancard", context);

        verify(user).setStateId(BotState.UnbanCard.ordinal());
        verify(messageService).sendMessage(123L, "You selected to unban card.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandInvalidOption() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("invalid_option", context);

        verify(messageService).sendMessage(123L, "Invalid option.");
    }
}
