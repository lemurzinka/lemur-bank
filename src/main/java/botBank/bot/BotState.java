package botBank.bot;


import botBank.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


public enum BotState {



    Start {

        @Override
        public void enter(BotContext context) {

                sendMessage(context, "Welcome!\n" +
                        "Hello, I am your personal assistant bot of Lemur Bank! Here to help you with all your banking needs. Let's get started by securing your account.");
        }

        @Override
        public BotState nextState() {
            return EnterPhone;
        }


    },

    EnterPhone {
        private boolean isWrongPhone;
        private int counterOfInputProblem = 0;
        private boolean isInputProblem;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please share your phone number so we can verify your identity and ensure a smooth experience.\n" +
                    "Just type your phone number below (including the country code, e.g., +1234567890).");
        }

        @Override
        public void handleInput(BotContext context) {
            String phoneNumber = context.getInput();

            if (!Utils.isValidPhoneNumber(phoneNumber)) {
                sendMessage(context, "Wrong phone number format!");
                isWrongPhone = true;
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    isInputProblem = true;
                    sendMessage(context, "Input problem, return to start.");
                }
                return;
            }
            isWrongPhone = false;
            counterOfInputProblem = 0;
                context.getUser().setNumber(phoneNumber);
                sendMessage(context, "Phone number saved.");

        }

        @Override
        public BotState nextState() {
            return isInputProblem ? Start : (isWrongPhone ? EnterPhone : EnterEmail);
        }
    },


    EnterEmail {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
            List<BotCommand> commands = List.of();
            sendMessage(context, "Enter your e-mail please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String email = context.getInput();

            if (Utils.isValidEmail(email)) {
                context.getUser().setEmail(context.getInput());
                next = Approved;
            } else {
                sendMessage(context, "Wrong e-mail address!");
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    sendMessage(context, "Input problem, return to start.");
                    next = Start;
                } else {
                    sendMessage(context, "Enter your e-mail please again");
                }

            }
        }


        @Override
        public BotState nextState() {
            return next;
        }
    },

    Approved(false) {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Email approved!");
        }

        @Override
        public BotState nextState() {
            return EnterFirstName;
        }
    },

    EnterFirstName {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your first name please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String firstName = context.getInput();
            context.getUser().setFirstName(firstName);
            sendMessage(context, "First name saved.");
        }

        @Override
        public BotState nextState() {
            return EnterLastName;
        }
    },

    EnterLastName {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your last name please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String lastName = context.getInput();
            User user = context.getUser();
            user.setLastName(lastName);


            sendMessage(context, "Last name saved. Next state is menu");
        }

        @Override
        public BotState nextState() {
            return Menu;
        }
    },


    Menu {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();


            InlineKeyboardButton updateButton = new InlineKeyboardButton("Update Email");
            updateButton.setCallbackData("/update");
            InlineKeyboardButton addCardButton = new InlineKeyboardButton("Add New Card");
            addCardButton.setCallbackData("/addcard");
            InlineKeyboardButton myCardsButton = new InlineKeyboardButton("My cards");
            myCardsButton.setCallbackData("/mycards");
            rows.add(List.of(updateButton, addCardButton, myCardsButton));

            if (context.getUser().isAdmin()) {
                InlineKeyboardButton listUsersButton = new InlineKeyboardButton("List Users");
                listUsersButton.setCallbackData("/listusers");
                InlineKeyboardButton banUserButton = new InlineKeyboardButton("Ban User");
                banUserButton.setCallbackData("/banuser");
                InlineKeyboardButton unbanUserButton = new InlineKeyboardButton("Unban User");
                unbanUserButton.setCallbackData("/unbanuser");
                rows.add(List.of(listUsersButton, banUserButton, unbanUserButton));
            }

            markup.setKeyboard(rows);
            sendMessageWithInlineKeyboard(context, "You are in the menu now. Choose an option:", markup);
        }


        @Override
        public void handleInput(BotContext context) {
            String wrongText = context.getInput();
            sendMessage(context, "Use buttons.");

            next = Menu;
        }


        @Override
        public BotState nextState() {
            return next;
        }
    },


    AddCard {
        private int counterOfInputProblem = 0;
        private BotState next;

        @Override
        public void enter(BotContext context){
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();


            InlineKeyboardButton creditCard = new InlineKeyboardButton("Credit");
            creditCard.setCallbackData("credit");
            InlineKeyboardButton debitCard = new InlineKeyboardButton("Debit");
            debitCard.setCallbackData("debit");
            rows.add(List.of(creditCard, debitCard));


            markup.setKeyboard(rows);
            sendMessageWithInlineKeyboard(context, "Chose a type of card:", markup);
        }


    @Override
    public void handleInput(BotContext context) {
        String wrongText = context.getInput();
        counterOfInputProblem ++;
        if (counterOfInputProblem > 3) {
            sendMessage(context, "Input problem, return to menu.");
            next = Menu;
        }
        sendMessage(context, "Use buttons.");

      next = AddCard;
    }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    BANNED{
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "You are banned from using the bot.");
        }

        @Override
        public BotState nextState() {
            return BANNED;
        }
    },

    BanUser {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter user ID to ban:");
        }

        @Override
        public void handleInput(BotContext context) {
            try {
                long userId = Long.parseLong(context.getInput());
                User userToBan = context.getUserService().findByTelegramId(userId);
                if (userToBan != null) {
                    userToBan.setBanned(true);
                    context.getUserService().updateUser(userToBan);
                    sendMessage(context, "User " + userId + " has been banned.");
                } else {
                    sendMessage(context, "User not found.");
                }
            } catch (NumberFormatException e) {
                sendMessage(context, "Invalid ID. Please try again.");
            }
        }

        @Override
        public BotState nextState() {
            return Menu;
        }
    },

    UnbanUser {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter user ID to unban:");
        }

        @Override
        public void handleInput(BotContext context) {
            try {
                long userId = Long.parseLong(context.getInput());
                User userToUnban = context.getUserService().findByTelegramId(userId);
                if (userToUnban != null) {
                    userToUnban.setBanned(false);
                    context.getUserService().updateUser(userToUnban);
                    sendMessage(context, "User " + userId + " has been unbanned.");
                } else {
                    sendMessage(context, "User not found.");
                }
            } catch (NumberFormatException e) {
                sendMessage(context, "Invalid ID. Please try again.");
            }
        }

        @Override
        public BotState nextState() {
            return Menu;
        }
    };


    public static void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);

        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    };



    private static BotState[] states;
    private final boolean inputNeeded;

    BotState() {
        this.inputNeeded = true;
    }

    BotState(boolean inputNeeded) {
        this.inputNeeded = inputNeeded;
    }

    public static BotState getInitialState() {
        return byId(0);
    }

    public static BotState byId(int id) {
        if (states == null) {
            states = BotState.values();
        }

        return states[id];
    }


    public abstract void enter(BotContext context);

    public abstract BotState nextState();

    public boolean isInputNeeded() {
        return inputNeeded;
    }

    public void handleInput(BotContext context) {
        // do nothing by default
    }
    private static void sendMessageWithInlineKeyboard(BotContext context, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);
        message.setReplyMarkup(markup);

        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}