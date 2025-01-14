package botBank.service;

import botBank.model.Account;
import botBank.model.Card;
import botBank.model.Transaction;
import botBank.model.TransactionType;
import botBank.repo.AccountRepository;
import botBank.repo.CardRepository;
import botBank.repo.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public String processTransaction(String senderCardNumber, String senderCvv, String senderExpDateStr,
                                     String recipientCardNumber, BigDecimal amount) {


        YearMonth senderExpDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            senderExpDate = YearMonth.parse(senderExpDateStr, formatter);
        } catch (DateTimeParseException e) {
            return "Invalid expiration date format. Use MM/YY.";
        }


        Optional<Card> senderCardOpt = cardRepository.findByCardNumber(senderCardNumber);
        if (senderCardOpt.isEmpty() || !senderCardOpt.get().getCvv().equals(senderCvv) ||
                !YearMonth.from(senderCardOpt.get().getExpirationDate()).equals(senderExpDate)) {
            return "Sender card details are invalid or not found.";
        }

        Account senderAccount = senderCardOpt.get().getAccount();
        if (senderAccount.getCurrentBalance().compareTo(amount) < 0) {
            return "Insufficient funds on sender's account.";
        }


        Optional<Card> recipientCardOpt = cardRepository.findByCardNumber(recipientCardNumber);
        Account recipientAccount = null;

        if (recipientCardOpt.isPresent()) {
            recipientAccount = recipientCardOpt.get().getAccount();
        }


        senderAccount.setCurrentBalance(senderAccount.getCurrentBalance().subtract(amount));
        accountRepository.save(senderAccount);

        if (recipientAccount != null) {
            recipientAccount.setCurrentBalance(recipientAccount.getCurrentBalance().add(amount));
            accountRepository.save(recipientAccount);
        }


        Transaction senderTransaction = new Transaction();
        senderTransaction.setAccount(senderAccount);
        senderTransaction.setTransactionType(TransactionType.TRANSFER);
        senderTransaction.setAmount(amount.negate());
        senderTransaction.setTransactionDate(LocalDateTime.now());

        if (recipientAccount != null) {
            senderTransaction.setRecipientAccount(recipientAccount);
        } else {
            senderTransaction.setRecipientDetails("External recipient: " + recipientCardNumber);
        }

        transactionRepository.save(senderTransaction);


        if (recipientAccount != null) {
            Transaction recipientTransaction = new Transaction();
            recipientTransaction.setAccount(recipientAccount);
            recipientTransaction.setTransactionType(TransactionType.TRANSFER);
            recipientTransaction.setAmount(amount);
            recipientTransaction.setTransactionDate(LocalDateTime.now());
            recipientTransaction.setRecipientAccount(senderAccount);
            transactionRepository.save(recipientTransaction);
        }

        return "Transaction successful.";
    }

   @Transactional
    public void saveTransaction(Transaction transaction){
        transactionRepository.save(transaction);
    }


}
