package tests;

import bot_bank.bot.BotContext;
import bot_bank.bot.ChatBot;
import bot_bank.model.Account;
import bot_bank.model.User;
import bot_bank.service.AccountListingService;
import bot_bank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountListingServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private BotContext botContext;

    @Mock
    private ChatBot bot;

    @Mock
    private User user;

    @InjectMocks
    private AccountListingService accountListingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(botContext.getUser()).thenReturn(user);
        when(botContext.getBot()).thenReturn(bot);
        when(user.getTelegramId()).thenReturn(123456789L);
    }

    @Test
    void testListAccounts_WithAccounts() throws TelegramApiException {
        Account account1 = new Account();
        account1.setAccountNumber("12345");
        account1.setCurrentBalance(BigDecimal.valueOf(1000));
        account1.setCurrency("USD");

        Account account2 = new Account();
        account2.setAccountNumber("67890");
        account2.setCurrentBalance(BigDecimal.valueOf(2000));
        account2.setCurrency("EUR");

        List<Account> accounts = Arrays.asList(account1, account2);
        when(accountService.findAllAccounts()).thenReturn(accounts);

        accountListingService.listAccounts(botContext);

        verify(accountService, times(1)).findAllAccounts();
        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testListAccounts_NoAccounts() throws TelegramApiException {
        List<Account> accounts = Collections.emptyList();
        when(accountService.findAllAccounts()).thenReturn(accounts);

        accountListingService.listAccounts(botContext);

        verify(accountService, times(1)).findAllAccounts();
        verify(bot, times(1)).execute(any(SendMessage.class));
    }
}
