package botBank.bot;

import botBank.model.User;
import botBank.service.UserService;

public class BotContext {
    private final ChatBot bot;
    private final User user;
    private final String input;
    private final UserService userService;

    public static BotContext of(ChatBot bot, User user, String text, UserService userService) {
        return new BotContext(bot, user, text, userService);
    }



    private BotContext(ChatBot bot, User user, String input, UserService userService) {
        this.bot = bot;
        this.user = user;
        this.input = input;
        this.userService = userService;
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

    public UserService getUserService() {
        return userService;
    }
}
