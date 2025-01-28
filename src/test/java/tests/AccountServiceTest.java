package tests;

import botBank.model.Account;
import botBank.model.User;
import botBank.repo.AccountRepository;
import botBank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;


    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void testSaveAccount() {
        Account account = new Account();
        account.setAccountNumber("123456789");
        account.setCurrentBalance(BigDecimal.valueOf(1000));
        accountService.saveAccount(account);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testVerifyAndSaveAccount() {
        Account account = new Account();
        account.setAccountNumber("123456789");
        when(accountRepository.existsByAccountNumber(account.getAccountNumber())).thenReturn(false);
        accountService.verifyAndSaveAccount(account);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testCreateAndSaveAccount() {
        User user = new User();
        Account account = accountService.createAccount(user);

        accountService.createAndSaveAccount(user);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertNotNull(capturedAccount.getAccountNumber());
        assertEquals(user, capturedAccount.getUser());
    }

    @Test
    void testFindAllAccounts() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());
        List<Account> accounts = accountService.findAllAccounts();
        assertNotNull(accounts);
        assertTrue(accounts.isEmpty());
        verify(accountRepository, times(1)).findAll();
    }


}
