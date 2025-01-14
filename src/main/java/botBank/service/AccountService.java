package botBank.service;
import botBank.model.Account;
import botBank.model.Card;
import botBank.model.User;
import botBank.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {


    private  final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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