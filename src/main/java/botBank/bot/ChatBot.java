package botBank.bot;

import botBank.model.Card;
import botBank.model.TransactionDetail;
import botBank.model.User;
import botBank.service.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class); //log4j

    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final CurrencyRateService rateService;
    private final TransactionService transactionService;
    private final AccountService accountService;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    public ChatBot(UserService userService, CardService cardService, CardAccountService cardAccountService, CurrencyRateService rateService, TransactionService transactionService, AccountService accountService) {
        this.userService = userService;
        this.cardService = cardService;
        this.cardAccountService = cardAccountService;
        this.rateService = rateService;
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
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

            BotContext context = BotContext.of(this, user, callbackData, messageId, userService, cardService, cardAccountService, transactionService, accountService, rateService);
            switch (callbackData) {
                case "/update":
                    LOGGER.info("User selected to update");
                    user.setStateId(BotState.EnterEmail.ordinal());
                    sendMessage(chatId, "You selected to update.");
                    break;
                case "/addcard":
                    LOGGER.info("User selected to add a card");
                    user.setStateId(BotState.AddCard.ordinal());
                    sendMessage(chatId, "You selected to add a card.");
                    break;
                case "/listusers":
                    LOGGER.info("User selected to list users");
                    userService.listUsers(context);
                    break;
                case "/banuser":
                    LOGGER.info("User selected to ban user");
                    user.setStateId(BotState.BanUser.ordinal());
                    sendMessage(chatId, "You selected to ban user.");
                    break;
                case "/unbanuser":
                    LOGGER.info("User selected to unban user");
                    user.setStateId(BotState.AddCard.ordinal());
                    sendMessage(chatId, "You selected to unban user.");
                    break;
                case "credit":
                    LOGGER.info("User selected to create a credit card");
                    context.getCardAccountService().createCreditCardAndAccount(user, BigDecimal.valueOf(5000), "UAH", context);
                    user.setStateId(BotState.Menu.ordinal());
                    break;
                case "debit":
                    LOGGER.info("User selected to add a debit card");
                    sendMessage(chatId, "You selected to add a debit card.");
                    user.setStateId(BotState.ChoseCurrency.ordinal());
                    break;
                case "UAH":
                case "USD":
                case "EUR":
                    LOGGER.info("User selected currency: {}", callbackData);
                    context.getCardAccountService().createDebitCardAndAccount(user, callbackData, context);
                    user.setStateId(BotState.Menu.ordinal());
                    break;
                case "/mycards":
                    LOGGER.info("User selected to view their cards");
                    displayUserCards(user, chatId);
                    break;
                case "/rates":
                    LOGGER.info("User selected to view rates");
                    String rateMessage = rateService.getFormattedRates();
                    sendMessage(chatId, rateMessage);
                    break;
                case "/send":
                    LOGGER.info("User selected to send money");
                    TransactionDetail transactionDetail = new TransactionDetail();
                    user.setTransactionDetail(transactionDetail);
                    user.setStateId(BotState.EnterCardNumberForTransaction.ordinal());
                    break;
                default:
                    LOGGER.warn("Invalid option selected by user");
                    updateUserState(user, BotState.Menu, "Invalid option.");
                    break;
            }

            userService.updateUser(user);

            BotState state = BotState.byId(user.getStateId());
            state.enter(context);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            final String text = update.getMessage().getText();
            final long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();

            LOGGER.info("Received message: {} from chat ID: {}", text, chatId);

            User user = userService.findByTelegramId(chatId);
            BotContext context = BotContext.of(this, user, text, messageId, userService, cardService, cardAccountService, transactionService, accountService, rateService);

            BotState state;

            if (user == null) {
                LOGGER.info("New user detected with chat ID: {}", chatId);
                state = BotState.getInitialState();
                user = new User(chatId, state.ordinal());
                userService.addUser(user);
                context = BotContext.of(this, user, text, messageId, userService, cardService, cardAccountService, transactionService, accountService, rateService);

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

    private void updateUserState(User user, BotState state, String message) {
        LOGGER.info("Updating user state to: {} with message: {}", state, message);
        user.setStateId(state.ordinal());
        sendMessage(user.getTelegramId(), message);
    }

    private void displayUserCards(User user, long chatId) {
        LOGGER.info("Displaying user cards for user ID: {}", user.getId());
        List<Card> userCards = cardService.getCardsByUserId(user.getId());
        if (userCards.isEmpty()) {
            sendMessage(chatId, "You have no cards.");
        } else {
            sendMessage(chatId, cardService.formatCardDetails(userCards));
        }
    }
}
