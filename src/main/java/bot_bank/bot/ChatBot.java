package bot_bank.bot;

import bot_bank.event.BotEvent;
import bot_bank.model.User;
import bot_bank.service.AccountService;
import bot_bank.service.BotExecutor;
import bot_bank.service.CardAccountService;
import bot_bank.service.CardService;
import bot_bank.service.CurrencyRateService;
import bot_bank.service.TransactionService;
import bot_bank.service.UserService;
import bot_bank.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChatBot is the core class responsible for handling updates from Telegram, processing user input,
 * and managing the bot's interactions with users.
 */

@Component
@PropertySource("classpath:telegram.properties")
@RequiredArgsConstructor
public class ChatBot extends TelegramLongPollingBot implements BotExecutor {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CurrencyRateService currencyRateService;
    private final ValidationService validationService;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;


    @Override
    public String getBotUsername() {
        return botName;
    }


    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void execute(SendMessage message) throws TelegramApiException {
        super.execute(message);
    }

    @Override
    public void onUpdateReceived(Update update) {
        executorService.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
        LOGGER.info("Received update: {}", update);

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();
            int messageId = callbackQuery.getMessage().getMessageId();

            LOGGER.info("Received callback query: {} from chat ID: {}", callbackData, chatId);

            User user = userService.findByTelegramId(chatId);

            if (user != null && user.isBanned()) {
                LOGGER.warn("User with chat ID: {} is banned", chatId);
                sendMessage(chatId, "You are banned. Please contact support.");
                return;
            }

            BotContext context = BotContext.of(this, user, callbackData, messageId, userService, cardService,
                    cardAccountService, transactionService, accountService, currencyRateService, validationService);
            eventPublisher.publishEvent(new BotEvent(this, context));

        } else if (update.hasMessage() && update.getMessage().hasText()) {
            final String text = update.getMessage().getText();
            final long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();

            LOGGER.info("Received message: {} from chat ID: {}", text, chatId);

            User user = userService.findByTelegramId(chatId);
            BotContext context = BotContext.of(this, user, text, messageId, userService, cardService,
                    cardAccountService, transactionService, accountService, currencyRateService, validationService);

            BotState state;

            if (user == null) {
                LOGGER.info("New user detected with chat ID: {}", chatId);
                state = BotState.getInitialState();
                user = new User(chatId, state.ordinal());
                userService.addUser(user);
                context = BotContext.of(this, user, text, messageId, userService, cardService, cardAccountService,
                        transactionService, accountService, currencyRateService, validationService);

                state.enter(context);
            } else {
                state = BotState.byId(user.getStateId());
                state.handleInput(context);
            }

            if (user.isBanned()) {
                LOGGER.warn("User with chat ID: {} is banned", chatId);
                sendMessage(chatId, "You are banned. Please contact support.");
                return;
            }

            do {
                state = state.nextState();
                state.enter(context);
            } while (!state.isInputNeeded());

            user.setStateId(state.ordinal());
            userService.updateUser(user);
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send message to chat ID: {}", chatId, e);
        }
    }
}
