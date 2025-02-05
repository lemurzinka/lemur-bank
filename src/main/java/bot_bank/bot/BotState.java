package bot_bank.bot;


import bot_bank.model.Account;
import bot_bank.model.Card;
import bot_bank.model.User;
import bot_bank.service.ValidationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * The BotState enum represents the various states that the bot can be in while interacting with users.
 * Each state corresponds to a specific step in the user journey, such as entering a command, providing
 * user information, or processing transactions. The enum helps manage and control the bot's behavior
 * based on the current state, ensuring a smooth and coherent user experience.
 * This enum also provides methods for transitioning between states, handling input based on the current
 * state, and defining actions to be taken when entering a new state.
 */

public enum BotState {



    START {
        private BotState next;


        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering Start state");
       InlineKeyboardMarkup markup = BotState.createHelloKeyboard();
            sendMessageWithInlineKeyboard(context, "Welcome!\n" +
                    "Hello, I am your personal assistant bot of Lemur Bank! Here to help you with all your banking needs. Let's get started by securing your account.", markup);
        }



        @Override
        public void handleInput(BotContext context) {
            LOGGER.warn("Invalid input received in Start state: {}", context.getInput());
                sendMessage(context, "Use buttons.");
                next = START;
        }


        @Override
        public BotState nextState() {
            return next;
        }


    },

    ENTER_PHONE {
        private boolean isWrongPhone;
        private int counterOfInputProblem = 0;
        private boolean isInputProblem;

        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterPhone state");
            sendMessage(context, "Please share your phone number so we can verify your identity and ensure a smooth experience.\n" +
                    "Just type your phone number below (you can include the country code, e.g., +1234567890).");
        }

        @Override
        public void handleInput(BotContext context) {
            String phoneNumber = context.getInput();
            ValidationService validationService = context.getValidationService();

            if (!validationService.isValidPhoneNumber(phoneNumber)) {
                LOGGER.warn("Invalid phone number format: {}", phoneNumber);
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
            if (isInputProblem) {
                return START;
            } else if (isWrongPhone) {
                return ENTER_PHONE;
            } else {
                return ENTER_EMAIL;
            }
        }

    },


    ENTER_EMAIL {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterEmail state");
            sendMessage(context, "Enter your e-mail please:");
        }

        @Override
        public void handleInput(BotContext context) {
            ValidationService validationService = context.getValidationService();
            String email = context.getInput();

            if (validationService.isValidEmail(email)) {
                LOGGER.info("Email {} is valid", email);
                context.getUser().setEmail(context.getInput());
                next = APPROVED;
            } else {
                LOGGER.warn("Invalid email format: {}", email);
                sendMessage(context, "Wrong e-mail address!");
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    sendMessage(context, "Input problem, return to start.");
                    next = START;
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

    APPROVED(false) {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering Approved state");
            sendMessage(context, "Email approved!");
        }

        @Override
        public BotState nextState() {
            return ENTER_FIRST_NAME;
        }
    },

    ENTER_FIRST_NAME {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterFirstName state");
            sendMessage(context, "Enter your first name please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String firstName = context.getInput();
            LOGGER.info("First name entered: {}", firstName);
            context.getUser().setFirstName(firstName);
            sendMessage(context, "First name saved.");
        }

        @Override
        public BotState nextState() {
            return ENTER_LAST_NAME;
        }
    },

    ENTER_LAST_NAME {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterLastName state");
            sendMessage(context, "Enter your last name please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String lastName = context.getInput();
            User user = context.getUser();
            LOGGER.info("Last name entered: {}", lastName);
            user.setLastName(lastName);


            sendMessage(context, "Last name saved. Next state is password");
        }

        @Override
        public BotState nextState() {
            return ENTER_PASSWORD;
        }
    },



        MENU {
            private BotState next;

            @Override
            public void enter(BotContext context) {
                LOGGER.info("Entering Menu state");
                InlineKeyboardMarkup markup = createMenuKeyboard(context.getUser().isAdmin());
                sendMessageWithInlineKeyboard(context, "You are in the menu now. Choose an option:", markup);
            }

            @Override
            public void handleInput(BotContext context) {
                LOGGER.warn("Invalid input received in Menu state: {}", context.getInput());
                sendMessage(context, "Use buttons.");
                next = MENU;
            }

            @Override
            public BotState nextState() {
                return next;
            }


        },

        ADD_CARD {
            private int counterOfInputProblem = 0;
            private BotState next;

            @Override
            public void enter(BotContext context) {
                LOGGER.info("Entering AddCard state");
                InlineKeyboardMarkup markup = createAddCardKeyboard();
              sendMessageWithInlineKeyboard(context, "Chose a type of card:", markup);
            }

            @Override
            public void handleInput(BotContext context) {
                LOGGER.warn("Invalid input received in AddCard state: {}", context.getInput());
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    LOGGER.warn("Input problem exceeded limit in AddCard state");
                  sendMessage(context, "Input problem, return to menu.");
                    next = MENU;
                } else {
                  sendMessage(context, "Use buttons.");
                    next = ADD_CARD;
                }
            }

            @Override
            public BotState nextState() {
                return next;
            }


        },

        BANNED {
            @Override
            public void enter(BotContext context) {
                LOGGER.info("Entering BANNED state");
                sendMessage(context, "You are banned from using the bot.");
            }

            @Override
            public BotState nextState() {
                return BANNED;
            }
        },

        BAN_USER {
            @Override
            public void enter(BotContext context) {
                LOGGER.info("Entering BanUser state");
               sendMessage(context, "Enter user ID to ban:");
            }

            @Override
            public void handleInput(BotContext context) {
                try {
                    long userId = Long.parseLong(context.getInput());
                    User userToBan = context.getUserService().findByTelegramId(userId);
                    if (userToBan != null) {
                        LOGGER.info("Banning user with ID: {}", userId);
                        userToBan.setBanned(true);
                        context.getUserService().updateUser(userToBan);
                       sendMessage(context, "User " + userId + " has been banned.");
                    } else {
                        LOGGER.warn("User with ID: {} not found (Ban)", userId);
                        sendMessage(context, "User not found.");
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("Invalid user ID format (Ban): {}", context.getInput(), e);
                    sendMessage(context, "Invalid ID. Please try again.");
                }
            }

            @Override
            public BotState nextState() {
                return MENU;
            }
        },

        UNBAN_USER {
            @Override
            public void enter(BotContext context) {
                LOGGER.info("Entering UnbanUser state");
                sendMessage(context, "Enter user ID to unban:");
            }

            @Override
            public void handleInput(BotContext context) {
                try {
                    long userId = Long.parseLong(context.getInput());
                    User userToUnban = context.getUserService().findByTelegramId(userId);
                    if (userToUnban != null) {
                        LOGGER.info("Unbanning user with ID: {}", userId);
                        userToUnban.setBanned(false);
                        context.getUserService().updateUser(userToUnban);
                        sendMessage(context, "User " + userId + " has been unbanned.");
                    } else {
                        LOGGER.warn("User with ID: {} not found (Unban)", userId);
                       sendMessage(context, "User not found.");
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("Invalid user ID format (Unban): {}", context.getInput(), e);
                   sendMessage(context, "Invalid ID. Please try again.");
                }
            }

            @Override
            public BotState nextState() {
                return MENU;
            }
        },

        CHOSE_CURRENCY {
            private int counterOfInputProblem = 0;
            private BotState next;

            @Override
            public void enter(BotContext context) {
                LOGGER.info("Entering ChoseCurrency state");
                InlineKeyboardMarkup markup = createCurrencyKeyboard();
                sendMessageWithInlineKeyboard(context, "What should be the currency of the card?:", markup);
            }

            @Override
            public void handleInput(BotContext context) {
                LOGGER.warn("Invalid input received in ChoseCurrency state: {}", context.getInput());
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    LOGGER.warn("Input problem exceeded limit in ChoseCurrency state");
                   sendMessage(context, "Input problem, return to menu.");
                    next = MENU;
                } else {
                   sendMessage(context, "Use buttons.");
                    next = CHOSE_CURRENCY;
                }
            }

            @Override
            public BotState nextState() {
                return next;
            }

        },




    ENTER_CARD_NUMBER_FOR_TRANSACTION {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterCardNumberForTransaction state");
            sendMessage(context, "Please, enter your card number for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cardNumber = context.getInput();
            LOGGER.info("Card number entered for transaction: {}", cardNumber);
            context.getUser().getTransactionDetail().setSenderCardNumber(cardNumber);
        }

        @Override
        public BotState nextState() {
            return ENTER_CVV_FOR_TRANSACTION;
        }
    },

    ENTER_CVV_FOR_TRANSACTION {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterCVVForTransaction state");
            sendMessage(context, "Please, enter your CVV for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cvv = context.getInput();
            LOGGER.info("CVV entered for transaction: {}", cvv);
            context.getUser().getTransactionDetail().setSenderCvv(cvv);
        }

        @Override
        public BotState nextState() {
            return ENTER_CARD_EXP_DATE_FOR_TRANSACTION;
        }
    },

    ENTER_CARD_EXP_DATE_FOR_TRANSACTION {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterCardExpDateForTransaction state");
            sendMessage(context, "Please, enter expiration date of your card for transaction (Format: yyyy-MM-dd):");
        }

        @Override
        public void handleInput(BotContext context) {
            String expDate = context.getInput();
            LOGGER.info("Expiration date entered for transaction: {}", expDate);
            context.getUser().getTransactionDetail().setSenderExpDate(expDate);
        }

        @Override
        public BotState nextState() {
            return ENTER_RECIPIENT_CARD_NUMBER_FOR_TRANSACTION;
        }
    },

    ENTER_RECIPIENT_CARD_NUMBER_FOR_TRANSACTION {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterRecipientCardNumberForTransaction state");
            sendMessage(context, "Please, enter recipient card number for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cardNumber = context.getInput();
            LOGGER.info("Recipient card number entered for transaction: {}", cardNumber);
            context.getUser().getTransactionDetail().setRecipientCardNumber(cardNumber);
        }

        @Override
        public BotState nextState() {
            return ENTER_PASSWORD_FOR_TRANSACTION;
        }
    },

    ENTER_AMOUNT_FOR_TRANSACTION_AND_MAKE_TRANSACTION {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterAmountForTransactionAndMakeTransaction state");
            sendMessage(context, "Please, enter amount for transaction (e.g., 100.50):");
        }


        @Override
        public void handleInput(BotContext context) {
            String amountStr = context.getInput();
            try {
                BigDecimal senderAmount = context.getTransactionService().parseAmount(amountStr, context);
                if (senderAmount == null) return;

                if (!context.getTransactionService().validateSenderDetails(context, senderAmount)) return;

                Optional<Card> senderCardOptional = context.getCardService().findByCardNumber(context.getUser().getTransactionDetail().getSenderCardNumber());
                if (senderCardOptional.isEmpty()) {
                    sendMessage(context, "Sender card not found.");
                    return;
                }

                Account senderAccount = senderCardOptional.get().getAccount();
                Account recipientAccount = context.getTransactionService().getRecipientAccount(context);

                BigDecimal recipientAmount = context.getTransactionService().convertCurrencyIfNeeded(senderAccount, recipientAccount, senderAmount, context);
                if (recipientAmount == null) return;

                context.getTransactionService().processTransaction(senderAccount, recipientAccount, senderAmount, recipientAmount, context);

                sendMessage(context, "Transaction is successful.");
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid amount format: {}", amountStr, e);
                sendMessage(context, "Invalid amount format. Please, enter a valid number (e.g., 100.50).");
            }
        }


        @Override
        public BotState nextState() {
            return MENU;
        }
    },


    ENTER_PASSWORD {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterPassword state");
            sendMessage(context, "Enter your password please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String password = context.getInput();
            int messageId = context.getMessageId();
            ValidationService validationService = context.getValidationService();

            if (validationService.isValidPassword(password)) {
                LOGGER.info("Valid password entered");
                String encodedPassword = validationService.encodePassword(password);
                context.getUser().setPassword(encodedPassword);
                sendMessage(context, "Password saved. Next state is Menu.");
                deleteMessage(context, messageId);
                next = MENU;
            } else {
                LOGGER.warn("Invalid password format");
                sendMessage(context, "Invalid password format! Use One big and small letter, one digit and one " +
                        "special symbol (no spaces!!!)");
                deleteMessage(context, messageId);
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    LOGGER.warn("Input problem exceeded limit in EnterPassword state");
                    sendMessage(context, "Input problem, return to start.");
                    next = START;
                } else {
                    sendMessage(context, "Enter your password again please:");
                }
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    ENTER_PASSWORD_FOR_TRANSACTION {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterPasswordForTransaction state");
            sendMessage(context, "Please, enter your password for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            ValidationService validationService = context.getValidationService();
            String password = context.getInput();
            int messageId = context.getMessageId();

            User user = context.getUser();
            String encodedPassword = user.getPassword();

            if (validationService.matchesPassword(password, encodedPassword)) {
                LOGGER.info("Password verified for transaction");
                sendMessage(context, "Password verified.");
                deleteMessage(context, messageId);
                next = ENTER_AMOUNT_FOR_TRANSACTION_AND_MAKE_TRANSACTION;
            } else {
                LOGGER.warn("Invalid password format or wrong password entered");
                sendMessage(context, "Invalid password format or wrong password");
                deleteMessage(context, messageId);
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    LOGGER.warn("Input problem exceeded limit in EnterPasswordForTransaction state");
                    sendMessage(context, "Input problem, return to Menu.");
                    next = MENU;
                } else {
                    sendMessage(context, "Enter your password again please:");
                }
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    ENTER_PASSWORD_FOR_UPDATE {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering EnterPasswordForUpdate state");
            sendMessage(context, "Please, enter your password for update:");
        }

        @Override
        public void handleInput(BotContext context) {
            ValidationService validationService = context.getValidationService();
            String password = context.getInput();
            int messageId = context.getMessageId();

            User user = context.getUser();
            String encodedPassword = user.getPassword();

            if (validationService.matchesPassword(password, encodedPassword)) {
                LOGGER.info("Password verified for update");
                sendMessage(context, "Password verified.");
                deleteMessage(context, messageId);
                next = ENTER_EMAIL;
            } else {
                LOGGER.warn("Invalid password format or wrong password entered");
                sendMessage(context, "Invalid password format or wrong password");
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    LOGGER.warn("Input problem exceeded limit in EnterPasswordForUpdate state");
                    sendMessage(context, "Input problem, return to Menu.");
                    deleteMessage(context, messageId);
                    next = MENU;
                } else {
                    sendMessage(context, "Enter your password again please:");
                }
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },
    BAN_CARD {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering BanCard state");
            sendMessage(context, "Enter card number to ban:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cardNumber = context.getInput();
            Card cardToBan = context.getCardService().findCardByCardNumber(cardNumber);
            if (cardToBan != null) {
                if (cardToBan.getUser().getId().equals(context.getUser().getId())) {
                    LOGGER.warn("Admin cannot ban their own card: {}", cardNumber);
                    sendMessage(context, "You cannot ban your own card.");
                } else {
                    LOGGER.info("Card before banning: {}", cardToBan);
                    LOGGER.info("Banning card with number: {}", cardNumber);
                    cardToBan.setBanned(true);
                    context.getCardService().updateCard(cardToBan);
                    LOGGER.info("Card after banning: {}", cardToBan);
                    sendMessage(context, "Card " + cardNumber + " has been banned.");
                }
            } else {
                LOGGER.warn("Card with number: {} not found (Ban)", cardNumber);
                sendMessage(context, "Card not found.");
            }
        }

        @Override
        public BotState nextState() {
            return MENU;
        }
    },

    UNBAN_CARD {
        @Override
        public void enter(BotContext context) {
            LOGGER.info("Entering UnbanCard state");
            sendMessage(context, "Enter card number to unban:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cardNumber = context.getInput();
            Card cardToUnban = context.getCardService().findCardByCardNumber(cardNumber);
            if (cardToUnban != null) {
                if (cardToUnban.getUser().getId().equals(context.getUser().getId())) {
                    LOGGER.warn("Admin cannot unban their own card: {}", cardNumber);
                    sendMessage(context, "You cannot unban your own card.");
                } else {
                    LOGGER.info("Card before unbanning: {}", cardToUnban);
                    LOGGER.info("Unbanning card with number: {}", cardNumber);
                    cardToUnban.setBanned(false);
                    context.getCardService().updateCard(cardToUnban);
                    LOGGER.info("Card after unbanning: {}", cardToUnban);
                    sendMessage(context, "Card " + cardNumber + " has been unbanned.");
                }
            } else {
                LOGGER.warn("Card with number: {} not found (Unban)", cardNumber);
                sendMessage(context, "Card not found.");
            }
        }

        @Override
        public BotState nextState() {
            return MENU;
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
    private static final Logger LOGGER = LogManager.getLogger(BotState.class);


    private static void deleteMessage(BotContext context, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(context.getUser().getTelegramId()));
        deleteMessage.setMessageId(messageId);
        try {
            context.getBot().execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private static InlineKeyboardMarkup createCurrencyKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton uahCurrencyButton = new InlineKeyboardButton();
        uahCurrencyButton.setText("UAH");
        uahCurrencyButton.setCallbackData("UAH");

        InlineKeyboardButton usdCurrencyButton = new InlineKeyboardButton();
        usdCurrencyButton.setText("USD");
        usdCurrencyButton.setCallbackData("USD");

        InlineKeyboardButton eurCurrencyButton = new InlineKeyboardButton();
        eurCurrencyButton.setText("EUR");
        eurCurrencyButton.setCallbackData("EUR");

        rows.add(List.of(uahCurrencyButton, usdCurrencyButton, eurCurrencyButton));

        markup.setKeyboard(rows);
        return markup;
    }



    private static InlineKeyboardMarkup createHelloKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton helloButton = new InlineKeyboardButton();
        helloButton.setText("Hello");
        helloButton.setCallbackData("/hi");



        rows.add(List.of(helloButton));

        markup.setKeyboard(rows);
        return markup;
    }



    private static InlineKeyboardMarkup createMenuKeyboard(boolean isAdmin) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton updateButton = new InlineKeyboardButton();
        updateButton.setText("Update data");
        updateButton.setCallbackData("/update");

        InlineKeyboardButton addCardButton = new InlineKeyboardButton();
        addCardButton.setText("Add New Card");
        addCardButton.setCallbackData("/addcard");

        InlineKeyboardButton myCardsButton = new InlineKeyboardButton();
        myCardsButton.setText("My cards");
        myCardsButton.setCallbackData("/mycards");

        InlineKeyboardButton exchangeRateButton = new InlineKeyboardButton();
        exchangeRateButton.setText("Exchange rate");
        exchangeRateButton.setCallbackData("/rates");

        InlineKeyboardButton sendMoneyButton = new InlineKeyboardButton();
        sendMoneyButton.setText("Send money");
        sendMoneyButton.setCallbackData("/send");

        rows.add(List.of(updateButton, addCardButton, myCardsButton));
        rows.add(List.of(exchangeRateButton, sendMoneyButton));

        if (isAdmin) {
            InlineKeyboardButton listUsersButton = new InlineKeyboardButton();
            listUsersButton.setText("List Users");
            listUsersButton.setCallbackData("/listusers");

            InlineKeyboardButton banUserButton = new InlineKeyboardButton();
            banUserButton.setText("Ban User");
            banUserButton.setCallbackData("/banuser");

            InlineKeyboardButton unbanUserButton = new InlineKeyboardButton();
            unbanUserButton.setText("Unban User");
            unbanUserButton.setCallbackData("/unbanuser");

            InlineKeyboardButton listAccountsButton = new InlineKeyboardButton();
            listAccountsButton.setText("List Accounts");
            listAccountsButton.setCallbackData("/listaccounts");

            InlineKeyboardButton listCardsButton = new InlineKeyboardButton();
            listCardsButton.setText("List Cards");
            listCardsButton.setCallbackData("/listcards");

            InlineKeyboardButton banCardButton = new InlineKeyboardButton();
            banCardButton.setText("Ban Card");
            banCardButton.setCallbackData("/bancard");

            InlineKeyboardButton unbanCardButton = new InlineKeyboardButton();
            unbanCardButton.setText("Unban Card");
            unbanCardButton.setCallbackData("/unbancard");

            rows.add(List.of(listUsersButton, banUserButton, unbanUserButton));
            rows.add(List.of(listAccountsButton, listCardsButton, banCardButton));
            rows.add(List.of(unbanCardButton));
        }

        markup.setKeyboard(rows);
        return markup;
    }


    private static InlineKeyboardMarkup createAddCardKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton creditCardButton = new InlineKeyboardButton();
        creditCardButton.setText("Credit");
        creditCardButton.setCallbackData("credit");

        InlineKeyboardButton debitCardButton = new InlineKeyboardButton();
        debitCardButton.setText("Debit");
        debitCardButton.setCallbackData("debit");

        rows.add(List.of(creditCardButton, debitCardButton));

        markup.setKeyboard(rows);
        return markup;
    }

}