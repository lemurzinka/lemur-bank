package botBank.service;

import botBank.model.Credit;
import botBank.repo.CreditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditService {

    private final CreditRepository creditRepository;

    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }


    @Transactional
    public void save(Credit credit) {
        creditRepository.save(credit);
    }



}
