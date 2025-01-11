package botBank.bot;

import botBank.model.Account;
import botBank.model.Card;
import botBank.model.User;
import botBank.retrievers.RateRetriever;
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
import java.util.Map;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class); //log4j

    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final CurrencyRateService rateService;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;



    public ChatBot(UserService userService, CardService cardService, CardAccountService cardAccountService, CurrencyRateService rateService) {
        this.userService = userService;
        this.cardService = cardService;
        this.cardAccountService = cardAccountService;
        this.rateService = rateService;
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


        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();

            User user = userService.findByTelegramId(chatId);

            if (user != null && user.isBanned()) {
                sendMessage(chatId, "You are banned. Please contact support.");
                return;
            }

            BotContext context = BotContext.of(this, user, callbackData, userService, cardService, cardAccountService);
            switch (callbackData) {
                case "/update":
                    user.setStateId(BotState.EnterEmail.ordinal());
                    sendMessage(chatId, "You selected to update.");
                    break;
                case "/addcard":
                    user.setStateId(BotState.AddCard.ordinal());
                    sendMessage(chatId, "You selected to add a card.");
                    break;
                case "/listusers":
                    userService.listUsers(context);
                    break;
                case "/banuser":
                    user.setStateId(BotState.BanUser.ordinal());
                    sendMessage(chatId, "You selected to ban user.");
                    break;
                case "/unbanuser":
                    user.setStateId(BotState.AddCard.ordinal());
                    sendMessage(chatId, "You selected to unban user.");
                case "credit":
                    context.getCardAccountService().createCreditCardAndAccount(user, BigDecimal.valueOf(5000), "UAH", context);
                    user.setStateId(BotState.Menu.ordinal());
                    break;
                case "debit":
                    sendMessage(chatId, "You selected to add a debit card.");
                    user.setStateId(BotState.ChoseCurrency.ordinal());
                    break;
                case "UAH":
                case "USD":
                case "EUR":
                    context.getCardAccountService().createDebitCardAndAccount(user, callbackData, context);
                    user.setStateId(BotState.Menu.ordinal());
                    break;

                case "/mycards":
                    displayUserCards(user, chatId);
                    break;

                case "/rates":
                    String rateMessage = rateService.getFormattedRates();
                    sendMessage(chatId, rateMessage);
                    break;


                default:
                    updateUserState(user, BotState.Menu, "Invalid option.");
                    break;
            }

            userService.updateUser(user);

            BotState state = BotState.byId(user.getStateId());
            state.enter(context);
        } else if (update.hasMessage() && update.getMessage().hasText()) {

            final String text = update.getMessage().getText();
            final long chatId = update.getMessage().getChatId();



            User user = userService.findByTelegramId(chatId);
            BotContext context = BotContext.of(this, user, text, userService, cardService, cardAccountService);


            BotState state;

            if (user == null) {
                state = BotState.getInitialState();
                user = new User(chatId, state.ordinal());
                userService.addUser(user);
                context = BotContext.of(this, user, text, userService, cardService, cardAccountService);

                state.enter(context);
            } else {
                state = BotState.byId(user.getStateId());
                state.handleInput(context);
            }



            if (user.isBanned()) {
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
            e.printStackTrace();
        }
    }


    private void updateUserState(User user, BotState state, String message) {
        user.setStateId(state.ordinal());
        sendMessage(user.getTelegramId(), message);
    }


    private void displayUserCards(User user, long chatId) {
        List<Card> userCards = cardService.getCardsByUserId(user.getId());
        if (userCards.isEmpty()) {
            sendMessage(chatId, "You have no cards.");
        } else {
            sendMessage(chatId, cardService.formatCardDetails(userCards));
        }
    }

}