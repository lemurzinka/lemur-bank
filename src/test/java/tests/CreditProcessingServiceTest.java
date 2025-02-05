package tests;

import bot_bank.model.Account;
import bot_bank.model.Credit;
import bot_bank.repo.AccountRepository;
import bot_bank.repo.CreditRepository;
import bot_bank.service.CreditProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditProcessingServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CreditProcessingService creditProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckAndApplyCredits_WithAccounts() {
        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setCurrentBalance(new BigDecimal(100));
        account.setCreditBalance(new BigDecimal(200));
        account.setLastCheckedDate(LocalDateTime.now().minusMonths(2));
        account.setCreatedAt(LocalDateTime.now().minusMonths(3));

        List<Account> accounts = Arrays.asList(account);

        when(accountRepository.findAllByCurrentBalanceLessThanCreditBalance()).thenReturn(accounts);

        creditProcessingService.checkAndApplyCredits();

        verify(accountRepository, times(1)).findAllByCurrentBalanceLessThanCreditBalance();
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCheckAndApplyCredits_NoAccounts() {
        List<Account> accounts = Arrays.asList();

        when(accountRepository.findAllByCurrentBalanceLessThanCreditBalance()).thenReturn(accounts);

        creditProcessingService.checkAndApplyCredits();

        verify(accountRepository, times(1)).findAllByCurrentBalanceLessThanCreditBalance();
        verify(creditRepository, times(0)).save(any(Credit.class));
        verify(accountRepository, times(0)).save(any(Account.class));
    }
}
