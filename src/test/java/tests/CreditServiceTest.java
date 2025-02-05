package tests;

import bot_bank.model.Credit;
import bot_bank.repo.AccountRepository;
import bot_bank.repo.CreditRepository;
import bot_bank.service.CreditProcessingService;
import bot_bank.service.CreditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * CreditServiceTest contains unit tests for the CreditService class. It verifies the correctness
 * of methods related to saving credits, checking and applying credits, and scheduling regular checks
 * for account balances.
 */

class CreditServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CreditProcessingService creditProcessingService;

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
    void testScheduledCheckAndApplyCredits() {
        creditService.scheduledCheckAndApplyCredits();

        verify(creditProcessingService, times(1)).checkAndApplyCredits();
    }
}
