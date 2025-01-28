package botBank.service;

import botBank.bot.BotContext;
import botBank.model.Account;
import botBank.model.Credit;
import botBank.model.User;
import botBank.repo.AccountRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static botBank.service.UserService.sendMessage;

@Service
public class AccountService {

    private static final Logger LOGGER = LogManager.getLogger(AccountService.class);

    @Autowired
    private final AccountRepository accountRepository;


    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;

    }



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
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);

        return account;
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
