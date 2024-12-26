package botBank.bot;

import botBank.model.User;
import botBank.service.UserService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);


    @Value("${bot.name}")
    private  String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UserService userService;


    public ChatBot(UserService userService) {this.userService = userService;}

    @Override
    public String getBotUsername() {return botName;}

    @Override
    public String getBotToken() {return botToken;}

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (!update.hasMessage() || !update.getMessage().hasText()) return;

            final String text = update.getMessage().getText();
            final long chatId = update.getMessage().getChatId();

            User user = userService.findByTelegramId(chatId);
            BotContext context;
            BotState state;

            if (user == null) {
                state = BotState.getInitialState();

                user = new User(chatId, state.ordinal());
                userService.addUser(user);

                context = BotContext.of(this, user, text, userService);
                state.enter(context);

            } else {
                context = BotContext.of(this, user, text, userService);
                state = BotState.byId(user.getStateId());
            }
            state.handleInput(context);

            do {
                state = state.nextState();
                state.enter(context);
            } while (!state.isInputNeeded());

            user.setStateId(state.ordinal());
            userService.updateUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
