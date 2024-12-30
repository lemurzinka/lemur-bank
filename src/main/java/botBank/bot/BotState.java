package botBank.bot;

import botBank.model.Card;
import botBank.model.User;
import botBank.service.CardService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


public enum BotState {

    Start {

        private BotState next;

        @Override
        public void enter(BotContext context) {

            User user = context.getUser();


            if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                sendMessage(context, "Welcome back, " + user.getFirstName());
                next = Menu;

            } else {
                sendMessage(context, "Welcome!\n" +
                        "Hello, I am your personal assistant bot of Lemur Bank! Here to help you with all your banking needs. Let's get started by securing your account.");
                next = EnterPhone;

            }

        }



        @Override
        public BotState nextState() {
            return next;
        }


    },

    EnterPhone {
        private boolean userFound;
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

            User user = context.getUserService().findByNumber(phoneNumber);
            if (user != null) {
                sendMessage(context, "Welcome back, " + user.getFirstName());
                userFound = true;
                context.getUser().setTelegramId(user.getTelegramId());
            } else {
                context.getUser().setNumber(phoneNumber);
                sendMessage(context, "Phone number saved. Proceeding with registration...");
                userFound = false;
            }
        }

        @Override
        public BotState nextState() {
            return isInputProblem ? Start : isWrongPhone ? EnterPhone : (userFound ? Start : EnterEmail);
        }
    },


    EnterEmail {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
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

            sendMessage(context, "You have successfully registered! Welcome to Lemur Bank.");
        }

        @Override
        public BotState nextState() {
            return Start;
        }
    },


    Menu{
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your command");
        }

        private BotState next;

        @Override
        public void handleInput(BotContext context) {
            String command = context.getInput().toLowerCase().trim();

            if (command.equals("update")){
                next = EnterEmail;
            }else if (command.equals("addcard")){
                next = AddCard;
            }
            else {
                sendMessage(context, "Invalid command. Please type 'update' to proceed.");
                next = Start;
            }
        }
        @Override
        public BotState nextState() {
            return next;
        }
    },

    AddCard {

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "What type of card it should be added (credit, debit)?");
        }

        @Override
        public void handleInput(BotContext context) {
            String type = context.getInput().toLowerCase().trim();


            if (type.equals("credit") || type.equals("debit")) {
                Card card = context.getCardService().createCard(type.toUpperCase());
                card.setUser(context.getUser());
                context.getCardService().addCard(card);
            }else {
                sendMessage(context, "Invalid type. Please choose either 'credit' or 'debit'.");
            }
        }

        @Override
        public BotState nextState() {
            return Menu;
        }
    };


    protected void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);

        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

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




}
