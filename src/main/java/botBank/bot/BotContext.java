package botBank.bot;

import botBank.model.User;
import botBank.service.CardService;
import botBank.service.UserService;

public class BotContext {
    private final ChatBot bot;
    private final User user;
    private final String input;
    private final UserService userService;
    private final CardService cardService;

    public static BotContext of(ChatBot bot, User user, String text, UserService userService, CardService cardService) {
        return new BotContext(bot, user, text, userService, cardService);
    }



    private BotContext(ChatBot bot, User user, String input,UserService userService, CardService cardService) {
        this.bot = bot;
        this.user = user;
        this.input = input;
        this.userService = userService;
        this.cardService = cardService;
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
}
