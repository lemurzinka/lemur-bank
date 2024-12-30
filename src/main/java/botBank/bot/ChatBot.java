package botBank.bot;

import botBank.model.User;
import botBank.service.CardService;
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
    private final CardService cardService;


    public ChatBot(UserService userService, CardService cardService) {
        this.userService = userService;
        this.cardService = cardService;
    }

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
                LOGGER.info("New user detected, initializing state to Start");
                state = BotState.Start;
                user = new User(chatId, state.ordinal());
                userService.addUser(user);

                context = BotContext.of(this, user, text, userService, cardService);
                state.enter(context);
                LOGGER.info("User created and state set to Start");

            } else {
                context = BotContext.of(this, user, text, userService, cardService);
                state = BotState.byId(user.getStateId());
                LOGGER.info("User found, current state: " + state.name());
            }
            state.handleInput(context);

            do {
                state = state.nextState();
                state.enter(context);
            } while (!state.isInputNeeded());

            user.setStateId(state.ordinal());
            userService.updateUser(user);
        } catch (Exception e) {
            LOGGER.error("Error processing update", e);
        }
    }



}
