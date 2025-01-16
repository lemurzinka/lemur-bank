package botBank.service;
import botBank.model.Account;
import botBank.model.Credit;
import botBank.model.User;
import botBank.repo.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AccountService {



    @Autowired
    private  final AccountRepository accountRepository;


    private final CreditService creditService;

    @Transactional
    public void checkAndApplyCredits() {

        List<Account> accounts = accountRepository.findAllByCurrentBalanceLessThanBalance();

        for (Account account : accounts) {

            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime lastCheckedDateTime = account.getLastCheckedDate() != null
                    ? account.getLastCheckedDate()
                    : account.getCreatedAt();

            // number of months between dates
            long monthsBetween = ChronoUnit.MONTHS.between(lastCheckedDateTime, currentDateTime);


            if (monthsBetween >= 1) {

                BigDecimal debt = account.getBalance().subtract(account.getCurrentBalance());

                if (debt.compareTo(BigDecimal.ZERO) > 0) {

                    BigDecimal interest = debt.multiply(new BigDecimal(0.05));

                    account.setCurrentBalance(account.getCurrentBalance().subtract(interest));

                    Credit credit = new Credit();
                    credit.setInterestRate(interest.doubleValue());
                    credit.setAmount(debt.add(interest).doubleValue());
                    credit.setStartDate(account.getCreatedAt());
                    credit.setEndDate(currentDateTime.plusMonths(1)); // final date in 1 month
                    credit.setAccount(account);

                    creditService.save(credit);
                }

                account.setLastCheckedDate(currentDateTime);
                accountRepository.save(account);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * *") // Every first day of every month
    @Transactional
    public void scheduledCheckAndApplyCredits() {
        checkAndApplyCredits();
    }



    public AccountService(AccountRepository accountRepository, CreditService creditService) {
        this.accountRepository = accountRepository;
        this.creditService = creditService;
    }

    @Transactional
    public void saveAccount(Account account){
        accountRepository.save(account);
    }

    @Transactional
    public void verifyAndSaveAccount(Account account) {

        while (accountRepository.existsByAccountNumber(account.getAccountNumber())) {
            account.setAccountNumber(generateAccountNumber()); // Generating of new number if duplicate
        }
        accountRepository.save(account);
    }




    public  String generateAccountNumber() {
        String bankCode = "522";
        String clientCode = String.valueOf(100000 + (int) (Math.random() * 900000));
        String partialAccount = bankCode + clientCode;

        int controlDigit = calculateControlDigit(partialAccount);

        return partialAccount + controlDigit;
    }

    private  int calculateControlDigit(String partialAccount) {
        //Modulo 10 (Luhn) Algorithm
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


    public Account createAccount(User user){

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);

        return account;
    }

    @Transactional
    public void createAndSaveAccount(User user) {
        Account account = createAccount(user);
        verifyAndSaveAccount(account);

    }


}


