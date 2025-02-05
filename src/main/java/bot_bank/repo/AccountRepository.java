package bot_bank.repo;

import bot_bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public  interface AccountRepository extends JpaRepository<Account, Long> {

 boolean existsByAccountNumber(String accountNumber);



 @Query("SELECT a FROM Account a WHERE a.currentBalance < a.creditBalance")
 List<Account> findAllByCurrentBalanceLessThanCreditBalance();

}
