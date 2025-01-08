package botBank.repo;

import botBank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public  interface AccountRepository extends JpaRepository<Account, Long> {

 boolean existsByAccountNumber(String accountNumber);
}
