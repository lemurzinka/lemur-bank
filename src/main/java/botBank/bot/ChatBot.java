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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class); //log4j

    private static final String LIST_USERS = "users";
    private static final String BAN = "ban";
    private static final String UNBAN = "unban";

    private final CardService cardService;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UserService userService;

    public ChatBot(UserService userService, CardService cardService) {
        this.userService = userService;
        this.cardService = cardService;
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

            BotContext context = BotContext.of(this, user, callbackData, cardService);
            switch (callbackData) {
                case "/update":
                    user.setStateId(BotState.EnterEmail.ordinal());
                    sendMessage(chatId, "You selected to update.");
                    break;
                case "/addcard":
                    user.setStateId(BotState.AddCard.ordinal());
                    sendMessage(chatId, "You selected to add a card.");
                    break;
                default:
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
            if (user != null && user.isBanned()) {
                sendMessage(chatId, "You are banned. Please contact support.");
                return;
            }

            BotContext context = BotContext.of(this, user, text, cardService);

            if (checkIfAdminCommand(user, text))
                return;

            BotState state;
            if (user == null) {
                state = BotState.getInitialState();
                user = new User(chatId, state.ordinal());
                userService.addUser(user);

                state.enter(context);
            } else {
                state = BotState.byId(user.getStateId());
                state.handleInput(context);
            }

            do {
                state = state.nextState();
                state.enter(context);
            } while (!state.isInputNeeded());

            user.setStateId(state.ordinal());
            userService.updateUser(user);
        }
    }



    private boolean checkIfAdminCommand(User user, String text) {
        if (user == null || !user.isAdmin())
            return false;

        if (text.equals(LIST_USERS)) {
            LOGGER.info("Admin command received: " + LIST_USERS);
            listUsers(user);
            return true;
        }

        if (text.startsWith(BAN)) {
            LOGGER.info("Admin command received: ban");
            banUser(user, text);
            return true;
        }

        if (text.startsWith(UNBAN)) {
            LOGGER.info("Admin command received: unban");
            unbanUser(user, text);
            return true;
        }

        return false;
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

    private void listUsers(User admin) {
        StringBuilder sb = new StringBuilder("All users list:\r\n");
        List<User> users = userService.findAllUsers();

        users.forEach(user -> sb.append(user.getTelegramId())
                .append(' ')
                .append(user.getNumber())
                .append(' ')
                .append(user.getEmail())
                .append("\r\n"));

        sendMessage(admin.getTelegramId(), sb.toString());
    }

    private void banUser(User admin, String text) {
        try {
            String[] commandParts = text.split(" ");
            if (commandParts.length != 2) {
                sendMessage(admin.getTelegramId(), "Invalid command format. Usage: ban <telegram_id>");
                return;
            }

            long telegramIdToBan = Long.parseLong(commandParts[1]);

            User userToBan = userService.findByTelegramId(telegramIdToBan);
            if (userToBan == null) {
                sendMessage(admin.getTelegramId(), "User not found.");
                return;
            }

            userToBan.setBanned(true);
            userService.updateUser(userToBan);

            sendMessage(admin.getTelegramId(), "User " + telegramIdToBan + " has been banned.");


            userToBan.setStateId(BotState.BANNED.ordinal());
            userService.updateUser(userToBan);
        } catch (NumberFormatException e) {
            sendMessage(admin.getTelegramId(), "Invalid telegram_id format.");
        }
    }

    private void unbanUser(User admin, String text) {
        try {
            String[] commandParts = text.split(" ");
            if (commandParts.length != 2) {
                sendMessage(admin.getTelegramId(), "Invalid command format. Usage: unban <telegram_id>");
                return;
            }

            long telegramIdToUnban = Long.parseLong(commandParts[1]);

            User userToUnban = userService.findByTelegramId(telegramIdToUnban);
            if (userToUnban == null) {
                sendMessage(admin.getTelegramId(), "User not found.");
                return;
            }

            userToUnban.setBanned(false);
            userService.updateUser(userToUnban);

            sendMessage(admin.getTelegramId(), "User " + telegramIdToUnban + " has been unbanned.");

        } catch (NumberFormatException e) {
            sendMessage(admin.getTelegramId(), "Invalid telegram_id format.");
        }
    }



}
