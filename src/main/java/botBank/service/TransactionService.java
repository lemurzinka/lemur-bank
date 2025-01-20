package botBank.service;

import botBank.model.Transaction;
import botBank.repo.TransactionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class TransactionService {

    private static final Logger LOGGER = LogManager.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public void saveTransaction(Transaction transaction) {
        LOGGER.info("Saving transaction: {}", transaction);
        transactionRepository.save(transaction);
    }
}
