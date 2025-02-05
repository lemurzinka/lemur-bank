package bot_bank.service;

import bot_bank.model.Account;
import bot_bank.model.Credit;
import bot_bank.repo.AccountRepository;
import bot_bank.repo.CreditRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * CreditProcessingService manages the credit processing operations.
 */
@Service
@AllArgsConstructor
public class CreditProcessingService {

    private static final Logger LOGGER = LogManager.getLogger(CreditProcessingService.class);

    private final CreditRepository creditRepository;
    private final AccountRepository accountRepository;

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
                    BigDecimal interest = debt.multiply(BigDecimal.valueOf(0.05));
                    account.setCurrentBalance(account.getCurrentBalance().subtract(interest));
                    LOGGER.debug("Interest applied: {}", interest);

                    Credit credit = Credit.builder()
                            .interestRate(interest.doubleValue())
                            .amount(debt.add(interest).doubleValue())
                            .startDate(account.getCreatedAt())
                            .endDate(currentDateTime.plusMonths(1))
                            .account(account)
                            .build();

                    creditRepository.save(credit);
                    LOGGER.info("Credit applied to account: {}", account.getAccountNumber());
                }

                account.setLastCheckedDate(currentDateTime);
                LOGGER.debug("Setting last checked date for account: {}", account.getAccountNumber());
                accountRepository.save(account);
                LOGGER.info("Account updated with last checked date: {}", account.getAccountNumber());
            }
        }
    }
}
