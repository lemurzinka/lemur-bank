package tests;

import bot_bank.model.Account;
import bot_bank.repo.AccountRepository;
import bot_bank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AccountServiceTest contains unit tests for the AccountService class. It verifies the correctness
 * of methods related to account operations such as saving, verifying, creating, and finding accounts.
 */


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
    void testFindAllAccounts() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());
        List<Account> accounts = accountService.findAllAccounts();
        assertNotNull(accounts);
        assertTrue(accounts.isEmpty());
        verify(accountRepository, times(1)).findAll();
    }


}
