package bot_bank.service;

import bot_bank.model.Credit;
import bot_bank.repo.CreditRepository;
import bot_bank.repo.AccountRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreditService provides methods for managing credit operations, including saving credits,
 * checking and applying interest to credits, and scheduling regular checks for account balances.
 * It interacts with CreditRepository and AccountRepository to perform database operations.
 */

@Service
@AllArgsConstructor
public class CreditService {

    private static final Logger LOGGER = LogManager.getLogger(CreditService.class);

    private final CreditRepository creditRepository;
    private final AccountRepository accountRepository;
    private final CreditProcessingService creditProcessingService;

    @Transactional
    public void save(Credit credit) {
        try {
            creditRepository.save(credit);
            LOGGER.info("Saving credit with id: {}", credit.getId());
        } catch (Exception e) {
            LOGGER.error("Error saving credit: {}", credit, e);
        }
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void scheduledCheckAndApplyCredits() {
        LOGGER.info("Scheduled check and apply credits started");
        creditProcessingService.checkAndApplyCredits();
    }
}
