package botBank.bot;


import botBank.model.TransactionDetail;
import botBank.model.User;
import botBank.service.*;

public class BotContext {
    private final ChatBot bot;
    private final User user;
    private final String input;
    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final TransactionService transactionService;
    private  final  AccountService accountService;
    private  final  CurrencyRateService currencyRateService;







    public static BotContext of(ChatBot bot, User user, String text, UserService userService,
                                CardService cardService, CardAccountService cardAccountService, TransactionService transactionService,
                                AccountService accountService, CurrencyRateService rateService
                            ) {
        return new BotContext(bot, user, text, userService, cardService, cardAccountService, transactionService, accountService, rateService);
    }


    private BotContext(ChatBot bot, User user, String input, UserService userService,
                       CardService cardService, CardAccountService cardAccountService, TransactionService transactionService,
                       AccountService accountService, CurrencyRateService rateService
                       ) {
        this.bot = bot;
        this.user = user;
        this.input = input;
        this.userService = userService;
        this.cardService = cardService;
        this.cardAccountService = cardAccountService;
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.currencyRateService = rateService;


    }


    public ChatBot getBot() {
        return bot;
    }

    public User getUser() {
        return user;
    }

    public String getInput() {
        return input;
    }


    public CardService getCardService() {
        return cardService;
    }

    public UserService getUserService() {
        return userService;
    }

    public CardAccountService getCardAccountService() {
        return cardAccountService;
    }


    public TransactionService getTransactionService() {
        return transactionService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public CurrencyRateService getCurrencyRateService() {
        return currencyRateService;
    }
}