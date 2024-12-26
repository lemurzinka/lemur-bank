package botBank.bot;

import botBank.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public enum BotState {

    Start {
        private boolean userFound;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Welcome!\n" +
                    "Hello, I am your personal assistant bot of Lemur Bank! Here to help you with all your banking needs. Let's get started by securing your account.\n" +
                    "\n" +
                    "Please share your phone number so we can verify your identity and ensure a smooth experience.\n" +
                    "\n" +
                    "Just type your phone number below (including the country code, e.g., +1234567890).");
        }

        @Override
        public void handleInput(BotContext context) {
            String phoneNumber = context.getInput();
            User user = context.getUserService().findByNumber(phoneNumber);

            if (user != null) {
                String name = user.getFirstName();
                sendMessage(context, "Welcome back, " + name);
                userFound = true;
            } else {
                context.getUser().setNumber(phoneNumber);
                sendMessage(context, "Phone number saved. Proceeding with registration...");
                userFound = false;
            }
        }

        @Override
        public BotState nextState() {
            if (userFound) {
                return Start;
            } else {
                return EnterEmail;
            }
        }
    },


    EnterEmail {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your e-mail please:");
        }

        @Override
        public void handleInput(BotContext context) {
            try {
                String email = context.getInput();

                if (Utils.isValidEmail(email)) {
                    context.getUser().setEmail(context.getInput());
                    next = Approved;
                } else {
                    sendMessage(context, "Wrong e-mail address!");
                    next = EnterEmail;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public BotState nextState() {return next;}},

    Approved(false){
        @Override
        public void enter(BotContext context) {sendMessage(context, "Email approved!");}

        @Override
        public BotState nextState(){
            return EnterFirstName;
        }
    },

    EnterFirstName{
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
        public BotState nextState(){
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
            context.getUser().setLastName(lastName);

            context.getUserService().addUser(user);
            sendMessage(context, "You have successfully registered! Welcome to Lemur Bank.");

        }
        @Override
        public BotState nextState(){
            return Start;
        }
    };


    protected void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);

        try{
            context.getBot().execute(message);
        }catch (TelegramApiException e){
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

    public abstract void enter (BotContext context);
    public abstract BotState nextState();
    public boolean isInputNeeded() {
        return inputNeeded;
    }
    public void handleInput(BotContext context) {
        // do nothing by default
    }
}
