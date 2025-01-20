package botBank.service;

import botBank.model.Credit;
import botBank.repo.CreditRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditService {

    private static final Logger LOGGER = LogManager.getLogger(CreditService.class);

    private final CreditRepository creditRepository;

    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Transactional
    public void save(Credit credit) {
        LOGGER.info("Saving credit: {}", credit);
        creditRepository.save(credit);
    }
}
