package botBank.service;

import botBank.model.Account;
import botBank.model.Credit;
import botBank.repo.AccountRepository;
import botBank.repo.CreditRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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


    @Transactional
    public void save(Credit credit) {
        try {
            creditRepository.save(credit);
            LOGGER.info("Saving credit with id: {}", credit.getId());
        } catch (Exception e) {
            LOGGER.error("Error saving credit: {}", credit, e);
        }
    }

    @Transactional
    public void checkAndApplyCredits() {
        LOGGER.info("Checking and applying credits");
        List<Account> accounts = accountRepository.findAllByCurrentBalanceLessThanCreditBalance();

        for (Account account : accounts) {
            LOGGER.debug("Processing account: {}", account.getAccountNumber());

            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime lastCheckedDateTime = account.getLastCheckedDate() != null
                    ? account.getLastCheckedDate()
                    : account.getCreatedAt();

            long monthsBetween = ChronoUnit.MONTHS.between(lastCheckedDateTime, currentDateTime);
            LOGGER.debug("Months between last check and now: {}", monthsBetween);

            if (monthsBetween >= 1) {
                BigDecimal debt = account.getCreditBalance().subtract(account.getCurrentBalance());
                LOGGER.debug("Debt calculated: {}", debt);

                if (debt.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal interest = debt.multiply(new BigDecimal(0.05));
                    account.setCurrentBalance(account.getCurrentBalance().subtract(interest));
                    LOGGER.debug("Interest applied: {}", interest);

                    Credit credit = Credit.builder()
                            .interestRate(interest.doubleValue())
                            .amount(debt.add(interest).doubleValue())
                            .startDate(account.getCreatedAt())
                            .endDate(currentDateTime.plusMonths(1))
                            .account(account)
                            .build();

                    save(credit);
                    LOGGER.info("Credit applied to account: {}", account.getAccountNumber());
                }

                account.setLastCheckedDate(currentDateTime);
                LOGGER.debug("Setting last checked date for account: {}", account.getAccountNumber());
                accountRepository.save(account);
                LOGGER.info("Account updated with last checked date: {}", account.getAccountNumber());
            }
        }
    }


    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void scheduledCheckAndApplyCredits() {
        LOGGER.info("Scheduled check and apply credits started");
        checkAndApplyCredits();
    }
}
