package tests;

import botBank.model.Account;
import botBank.model.Credit;
import botBank.model.User;
import botBank.repo.AccountRepository;
import botBank.service.AccountService;
import botBank.service.CreditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CreditService creditService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository, creditService);
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

    @Test
    void testCheckAndApplyCredits() {
        Account account = new Account();
        account.setAccountNumber("123456789");
        account.setCurrentBalance(BigDecimal.valueOf(500));
        account.setCreditBalance(BigDecimal.valueOf(1000));
        account.setCreatedAt(LocalDateTime.now().minusMonths(2));

        when(accountRepository.findAllByCurrentBalanceLessThanCreditBalance()).thenReturn(Collections.singletonList(account));

        accountService.checkAndApplyCredits();

        verify(accountRepository, times(1)).save(account);
        verify(creditService, times(1)).save(any(Credit.class));
    }
}
