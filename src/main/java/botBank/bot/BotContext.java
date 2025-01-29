package botBank.bot;

import botBank.model.User;
import botBank.service.AccountService;
import botBank.service.CardAccountService;
import botBank.service.CardService;
import botBank.service.CurrencyRateService;
import botBank.service.TransactionService;
import botBank.service.UserService;
import botBank.service.ValidationService;
import lombok.Getter;


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