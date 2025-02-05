package bot_bank.bot;

import bot_bank.model.User;
import bot_bank.service.AccountService;
import bot_bank.service.CardAccountService;
import bot_bank.service.CardService;
import bot_bank.service.CurrencyRateService;
import bot_bank.service.TransactionService;
import bot_bank.service.UserService;
import bot_bank.service.ValidationService;
import lombok.Getter;

import java.io.Serializable;

/**
 * BotContext class holds the context required for processing interactions between the bot and the user.
 * It encapsulates the bot instance, the user, the input text, and various services needed for operations.
 * This class facilitates access to these components in a centralized manner, enhancing code organization
 * and maintainability.
 */

@Getter
public class BotContext {
    private final ChatBot bot;
    private final User user;
    private final String input;
    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CurrencyRateService currencyRateService;
    private final ValidationService validationService;
    private final Integer messageId;

    public static BotContext of(ChatBot bot, User user, String text, Integer messageId, UserService userService,
                                CardService cardService, CardAccountService cardAccountService, TransactionService transactionService,
                                AccountService accountService, CurrencyRateService rateService, ValidationService validationService) {
        return new BotContext(bot, user, text, messageId, userService, cardService, cardAccountService, transactionService, accountService, rateService,
                validationService);
    }

    private BotContext(ChatBot bot, User user, String input, Integer messageId, UserService userService,
                       CardService cardService, CardAccountService cardAccountService, TransactionService transactionService,
                       AccountService accountService, CurrencyRateService rateService, ValidationService validationService) {
        this.bot = bot;
        this.user = user;
        this.input = input;
        this.messageId = messageId;
        this.userService = userService;
        this.cardService = cardService;
        this.cardAccountService = cardAccountService;
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.currencyRateService = rateService;
        this.validationService = validationService;
    }

}