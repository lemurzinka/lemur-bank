package tests;

import bot_bank.bot.BotContext;
import bot_bank.bot.BotState;
import bot_bank.model.Account;
import bot_bank.model.Card;
import bot_bank.model.CardType;
import bot_bank.model.TransactionDetail;
import bot_bank.model.User;
import bot_bank.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import bot_bank.bot.ChatBot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CommandServiceTest contains unit tests for the CommandService class. It verifies the correctness
 * of methods related to handling user commands, such as updating user details, adding cards,
 * listing users, banning/unbanning users and cards, managing credits, and handling currency.
 */


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
    private AccountListingService accountListingService;

    @Mock
    private MessageService messageService;

    @Mock
    private  CardServiceFacade cardServiceFacade;

    @Mock
    private  UserListingService userListingService;

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
        commandService = new CommandService(userService, cardService, cardAccountService, rateService,
                transactionService, accountListingService, messageService, cardServiceFacade, userListingService);
        when(context.getUser()).thenReturn(user);
        when(context.getCardAccountService()).thenReturn(cardAccountService);
        when(context.getBot()).thenReturn(bot);
    }

    @Test
    void testHandleCommandUpdate() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/update", context);

        verify(user).setStateId(BotState.ENTER_PASSWORD_FOR_UPDATE.ordinal());
        verify(messageService).sendMessage(123L, "You selected to update.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandAddCard() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/addcard", context);

        verify(user).setStateId(BotState.ADD_CARD.ordinal());
        verify(messageService).sendMessage(123L, "You selected to add a card.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandListUsers() {
        commandService.handleCommand("/listusers", context);

        verify(userListingService).listUsers(context);
    }

    @Test
    void testHandleCommandBanUser() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/banuser", context);

        verify(user).setStateId(BotState.BAN_USER.ordinal());
        verify(messageService).sendMessage(123L, "You selected to ban user.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandUnbanUser() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/unbanuser", context);

        verify(user).setStateId(BotState.UNBAN_USER.ordinal());
        verify(messageService).sendMessage(123L, "You selected to unban user.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandCredit() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("credit", context);

        verify(cardAccountService).createCreditCardAndAccount(user, BigDecimal.valueOf(5000),
                "UAH", context);

        verify(user).setStateId(BotState.MENU.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandDebit() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("debit", context);

        verify(messageService).sendMessage(123L, "You selected to add a debit card.");
        verify(user).setStateId(BotState.CHOSE_CURRENCY.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandCurrency() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("USD", context);

        verify(cardAccountService).createDebitCardAndAccount(user, "USD",
                context);
        verify(user).setStateId(BotState.MENU.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandMyCards_NoCards() {

        when(user.getTelegramId()).thenReturn(123L);
        when(user.getId()).thenReturn(1L);
        when(cardService.getCardsByUserId(1L)).thenReturn(List.of());

        commandService.handleCommand("/mycards", context);


        verify(messageService).sendMessage(123L, "No cards now");
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
        verify(user).setStateId(BotState.ENTER_CARD_NUMBER_FOR_TRANSACTION.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandHi() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/hi", context);

        verify(user).setStateId(BotState.ENTER_PHONE.ordinal());
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandListCards() {
        commandService.handleCommand("/listcards", context);

        verify(cardServiceFacade).listCards(context);
    }

    @Test
    void testHandleCommandListAccounts() {
        commandService.handleCommand("/listaccounts", context);

        verify(accountListingService).listAccounts(context);
    }

    @Test
    void testHandleCommandBanCard() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/bancard", context);

        verify(user).setStateId(BotState.BAN_CARD.ordinal());
        verify(messageService).sendMessage(123L, "You selected to ban card.");
        verify(userService).updateUser(user);
    }

    @Test
    void testHandleCommandUnbanCard() {
        when(user.getTelegramId()).thenReturn(123L);

        commandService.handleCommand("/unbancard", context);

        verify(user).setStateId(BotState.UNBAN_CARD.ordinal());
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
