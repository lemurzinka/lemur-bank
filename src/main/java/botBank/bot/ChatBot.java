package botBank.bot;

import botBank.model.Account;
import botBank.model.Card;
import botBank.model.User;
import botBank.service.AccountService;
import botBank.service.CardService;
import botBank.service.UserService;
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
import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class); //log4j

    private final UserService userService;
    private final CardService cardService;
    private final AccountService accountService;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;



    public ChatBot(UserService userService, CardService cardService, AccountService accountService) {
        this.userService = userService;
        this.cardService = cardService;
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


        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();

            User user = userService.findByTelegramId(chatId);

            if (user != null && user.isBanned()) {
                sendMessage(chatId, "You are banned. Please contact support.");
                return;
            }

            BotContext context = BotContext.of(this, user, callbackData, userService, cardService, accountService);
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
                    Card creditCard = context.getCardService().createCard(callbackData.toUpperCase());
                    Account accountCredit = context.getAccountService().createAccount(user);
                    context.getAccountService().verifyAndSaveAccount(accountCredit);
                    creditCard.setUser(user);
                    creditCard.setAccount(accountCredit);
                    context.getCardService().addCard(creditCard);
                    sendMessage(chatId,"You have created a credit card, you can view its data in the menu");
                    user.setStateId(BotState.Menu.ordinal());
                    break;
                case "debit":
                    Card debitCard = context.getCardService().createCard(callbackData.toUpperCase());
                    Account accountDebit = context.getAccountService().createAccount(user);
                    context.getAccountService().verifyAndSaveAccount(accountDebit);
                    debitCard.setUser(user);
                    debitCard.setAccount(accountDebit);
                    context.getCardService().addCard(debitCard);
                    sendMessage(chatId,"You have created a debit card, you can view its data in the menu");
                    user.setStateId(BotState.Menu.ordinal());
                    break;

                case "/mycards":
                    List<Card> userCards = cardService.getCardsByUserId(user.getId());
                    if (userCards.isEmpty()) {
                        sendMessage(chatId, "You have no cards.");
                    } else {
                        String response = cardService.formatCardDetails(userCards);
                        sendMessage(chatId, response.toString());
                    }
                    break;

                default:
                    user.setStateId(BotState.Menu.ordinal());
                    sendMessage(chatId, "Invalid option.");
                    break;
            }

            userService.updateUser(user);

            BotState state = BotState.byId(user.getStateId());
            state.enter(context);
        } else if (update.hasMessage() && update.getMessage().hasText()) {

            final String text = update.getMessage().getText();
            final long chatId = update.getMessage().getChatId();



            User user = userService.findByTelegramId(chatId);
            BotContext context = BotContext.of(this, user, text, userService, cardService, accountService);


            BotState state;

            if (user == null) {
                state = BotState.getInitialState();
                user = new User(chatId, state.ordinal());
                userService.addUser(user);
                context = BotContext.of(this, user, text, userService, cardService, accountService);

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


}
