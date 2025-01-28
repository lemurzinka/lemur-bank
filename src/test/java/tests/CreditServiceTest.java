package tests;


import botBank.model.Account;
import botBank.model.Credit;
import botBank.repo.AccountRepository;
import botBank.repo.CreditRepository;
import botBank.service.CreditService;
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

class CreditServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CreditService creditService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        Credit credit = new Credit();
        credit.setId(1L);


        creditService.save(credit);


        verify(creditRepository, times(1)).save(credit);
    }

    @Test
    void testCheckAndApplyCredits() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setCurrentBalance(new BigDecimal(100));
        account.setCreditBalance(new BigDecimal(200));
        account.setLastCheckedDate(LocalDateTime.now().minusMonths(2));
        account.setCreatedAt(LocalDateTime.now().minusMonths(3));

        List<Account> accounts = Arrays.asList(account);


        when(accountRepository.findAllByCurrentBalanceLessThanCreditBalance()).thenReturn(accounts);


        creditService.checkAndApplyCredits();


        verify(accountRepository, times(1)).findAllByCurrentBalanceLessThanCreditBalance();
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testScheduledCheckAndApplyCredits() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setCurrentBalance(new BigDecimal(100));
        account.setCreditBalance(new BigDecimal(200));
        account.setLastCheckedDate(LocalDateTime.now().minusMonths(2));
        account.setCreatedAt(LocalDateTime.now().minusMonths(3));

        List<Account> accounts = Arrays.asList(account);

        // Імітація поведінки accountRepository
        when(accountRepository.findAllByCurrentBalanceLessThanCreditBalance()).thenReturn(accounts);

        // Виклик методу, що тестується
        creditService.scheduledCheckAndApplyCredits();

        // Перевірка правильності викликів
        verify(accountRepository, times(1)).findAllByCurrentBalanceLessThanCreditBalance();
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

}
