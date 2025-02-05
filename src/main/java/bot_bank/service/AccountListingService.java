package bot_bank.service;

import bot_bank.bot.BotContext;
import bot_bank.model.Account;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static bot_bank.service.UserListingService.sendMessage;


/**
 * AccountListingService provides methods for listing bank accounts. It interacts with the
 * AccountService to retrieve all accounts from the database and sends the account details
 * to the user via the Telegram bot.
 */

@Service
@AllArgsConstructor
public class AccountListingService {

    private static final Logger LOGGER = LogManager.getLogger(AccountListingService.class);


    private AccountService accountService;

    public void listAccounts(BotContext context) {
        StringBuffer sb = new StringBuffer("All accounts list:\r\n");
        List<Account> accounts = accountService.findAllAccounts();

        if (accounts.isEmpty()) {
            sb.append("No accounts found.");
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
