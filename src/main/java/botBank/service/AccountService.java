package botBank.service;


import botBank.bot.BotContext;
import botBank.model.Account;
import botBank.model.User;
import botBank.repo.AccountRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static botBank.service.UserService.sendMessage;

/**
 * AccountService provides methods for managing bank accounts, including creating, saving, verifying,
 * and listing accounts. It also handles account number generation and ensuring unique account numbers.
 * This service interacts with the AccountRepository to perform database operations.
 */

@Service
@AllArgsConstructor
public class AccountService {

    private static final Logger LOGGER = LogManager.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    @Transactional
    public void saveAccount(Account account) {
        LOGGER.info("Saving account: {}", account.getAccountNumber());
        accountRepository.save(account);
    }

    @Transactional
    public void verifyAndSaveAccount(Account account) {
        LOGGER.info("Verifying and saving account: {}", account.getAccountNumber());

        while (accountRepository.existsByAccountNumber(account.getAccountNumber())) {
            LOGGER.warn("Duplicate account number found, generating new number: {}", account.getAccountNumber());
            account.setAccountNumber(generateAccountNumber());
        }
        accountRepository.save(account);
    }

    public String generateAccountNumber() {
        String bankCode = "522";
        String clientCode = String.valueOf(100000 + (int) (Math.random() * 900000));
        String partialAccount = bankCode + clientCode;

        int controlDigit = calculateControlDigit(partialAccount);

        return partialAccount + controlDigit;
    }

    private int calculateControlDigit(String partialAccount) {
        int sum = 0;
        for (int i = 0; i < partialAccount.length(); i++) {
            int digit = Character.getNumericValue(partialAccount.charAt(i));
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }
        return (10 - (sum % 10)) % 10;
    }

    public Account createAccount(User user) {
        LOGGER.info("Creating account for user: {}", user.getId());

        return Account.builder()
                .accountNumber(generateAccountNumber())
                .user(user)
                .build();
    }

    @Transactional
    public void createAndSaveAccount(User user) {
        LOGGER.info("Creating and saving account for user: {}", user.getId());
        Account account = createAccount(user);
        verifyAndSaveAccount(account);
    }

    @Transactional(readOnly = true)
    public List<Account> findAllAccounts() {
        LOGGER.info("Finding all accounts");
        return accountRepository.findAll();
    }

    public void listAccounts(BotContext context) {
        StringBuffer sb = new StringBuffer("All accounts list:\r\n");
        List<Account> accounts = findAllAccounts();

        if (accounts.isEmpty()) {
            sb.append("No cards found.");
        }

        accounts.forEach(account -> sb.append(account.getAccountNumber())
                .append(" ")
                .append(account.getCurrentBalance())
                .append(" ")
                .append(account.getCurrency())
                .append("\r\n"));

        LOGGER.info("Listing all accounts");
        sendMessage(context, sb.toString());
    }
}
