package botBank.bot;

import botBank.model.User;
import botBank.service.AccountService;
import botBank.service.CardAccountService;
import botBank.service.CardService;
import botBank.service.UserService;

public class BotContext {
    private final ChatBot bot;
    private final User user;
    private final String input;
    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;


    public static BotContext of(ChatBot bot, User user, String text, UserService userService, CardService cardService, CardAccountService cardAccountService) {
        return new BotContext(bot, user, text, userService, cardService, cardAccountService);
    }



    private BotContext(ChatBot bot, User user, String input,UserService userService, CardService cardService, CardAccountService cardAccountService) {
        this.bot = bot;
        this.user = user;
        this.input = input;
        this.userService = userService;
        this.cardService = cardService;
        this.cardAccountService = cardAccountService;
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

}