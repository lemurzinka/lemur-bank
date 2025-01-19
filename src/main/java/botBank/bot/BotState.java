package botBank.bot;


import botBank.model.*;
import botBank.service.CurrencyRateService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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


            sendMessage(context, "Last name saved. Next state is password");
        }

        @Override
        public BotState nextState() {
            return EnterPassword;
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
            InlineKeyboardButton exchangeRate = new InlineKeyboardButton("Exchange rate");
            exchangeRate.setCallbackData("/rates");
            InlineKeyboardButton sendMoney = new InlineKeyboardButton("Send money");
            sendMoney.setCallbackData("/send");
            rows.add(List.of(updateButton, addCardButton, myCardsButton, exchangeRate, sendMoney));

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
    },

    ChoseCurrency {
        private int counterOfInputProblem = 0;
        private BotState next;

        @Override
        public void enter(BotContext context){
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();


            InlineKeyboardButton uahCurrency = new InlineKeyboardButton("UAH");
            uahCurrency.setCallbackData("UAH");
            InlineKeyboardButton usdCurrency = new InlineKeyboardButton("USD");
            usdCurrency.setCallbackData("USD");
            InlineKeyboardButton eurCurrency = new InlineKeyboardButton("EUR");
            eurCurrency.setCallbackData("EUR");
            rows.add(List.of(uahCurrency, usdCurrency, eurCurrency));


            markup.setKeyboard(rows);
            sendMessageWithInlineKeyboard(context, "What should be the currency of the card?:", markup);
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

            next = ChoseCurrency;
        }
        @Override
        public BotState nextState() {
            return next;
        }

    },

    EnterCardNumberForTransaction {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please, enter your card number for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cardNumber = context.getInput();
            context.getUser().getTransactionDetail().setSenderCardNumber(cardNumber);
        }

        @Override
        public BotState nextState() {
            return EnterCVVForTransaction;
        }
    },

    EnterCVVForTransaction {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please, enter your CVV for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String CVV = context.getInput();
            context.getUser().getTransactionDetail().setSenderCvv(CVV);
        }

        @Override
        public BotState nextState() {
            return EnterCardExpDateForTransaction;
        }
    },

    EnterCardExpDateForTransaction {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please, enter expiration date of your card for transaction (Format: yyyy-MM-dd):");
        }

        @Override
        public void handleInput(BotContext context) {
            String expDate = context.getInput();
            context.getUser().getTransactionDetail().setSenderExpDate(expDate);
        }

        @Override
        public BotState nextState() {
            return EnterRecipientCardNumberForTransaction;
        }
    },

    EnterRecipientCardNumberForTransaction {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please, enter recipient card number for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String cardNumber = context.getInput();
            context.getUser().getTransactionDetail().setRecipientCardNumber(cardNumber);
        }

        @Override
        public BotState nextState() {
            return EnterPasswordForTransaction;
        }
    },

    EnterAmountForTransactionAndMakeTransaction {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please, enter amount for transaction (e.g., 100.50):");
        }

        @Override
        public void handleInput(BotContext context) {
            String amountStr = context.getInput();
            try {
                BigDecimal senderAmount = new BigDecimal(amountStr);
                if (senderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    sendMessage(context, "The amount must be greater than zero. Please, try again.");
                    return;
                }
                context.getUser().getTransactionDetail().setAmount(senderAmount);
                String senderCardNumber = context.getUser().getTransactionDetail().getSenderCardNumber();
                String senderCvv = context.getUser().getTransactionDetail().getSenderCvv();
                String senderExpDateStr = context.getUser().getTransactionDetail().getSenderExpDate();
                String recipientCardNumber = context.getUser().getTransactionDetail().getRecipientCardNumber();

                LocalDate senderExpDate;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    senderExpDate = LocalDate.parse(senderExpDateStr, formatter);
                } catch (DateTimeParseException e) {
                    sendMessage(context, "Invalid expiration date format. Use yyyy-MM-dd.");
                    return;
                }

                Optional<Card> senderCardOpt = context.getCardService().findByCardNumber(senderCardNumber);
                if (senderCardOpt.isEmpty() ||
                        !senderCardOpt.get().getCvv().equals(senderCvv) ||
                        !senderCardOpt.get().getExpirationDate().equals(senderExpDate)) {
                    sendMessage(context, "Sender card details are invalid or not found.");
                    return;
                }

                Account senderAccount = senderCardOpt.get().getAccount();
                if (senderAccount.getCurrentBalance().compareTo(senderAmount) < 0) {
                    sendMessage(context, "Insufficient funds on sender's account.");
                    return;
                }

                Optional<Card> recipientCardOpt = context.getCardService().findByCardNumber(recipientCardNumber);
                Account recipientAccount = recipientCardOpt.map(Card::getAccount).orElse(null);

                BigDecimal recipientAmount = senderAmount;
                String senderCurrency = senderAccount.getCurrency();
                String recipientCurrency = recipientAccount != null ? recipientAccount.getCurrency() : null;

                if (recipientAccount != null && !senderCurrency.equals(recipientCurrency)) {
                    CurrencyRateService rateService = context.getCurrencyRateService();
                    Double rate = rateService.getRate(senderCurrency, recipientCurrency);
                    if (rate != null) {
                        recipientAmount = senderAmount.multiply(BigDecimal.valueOf(rate));
                    } else {
                        sendMessage(context, "Currency conversion rate is unavailable.");
                        return;
                    }
                }

                senderAccount.setCurrentBalance(senderAccount.getCurrentBalance().subtract(senderAmount));
                context.getAccountService().saveAccount(senderAccount);

                if (recipientAccount != null) {
                    recipientAccount.setCurrentBalance(recipientAccount.getCurrentBalance().add(recipientAmount));
                    context.getAccountService().saveAccount(recipientAccount);
                }

                Transaction senderTransaction = new Transaction();
                senderTransaction.setAccount(senderAccount);
                senderTransaction.setTransactionType(TransactionType.TRANSFER);
                senderTransaction.setAmount(senderAmount.negate());
                senderTransaction.setTransactionDate(LocalDateTime.now());

                if (recipientAccount != null) {
                    senderTransaction.setRecipientAccount(recipientAccount);
                } else {
                    senderTransaction.setRecipientDetails("External recipient: " + recipientCardNumber);
                }
                context.getTransactionService().saveTransaction(senderTransaction);

                if (recipientAccount != null) {
                    Transaction recipientTransaction = new Transaction();
                    recipientTransaction.setAccount(recipientAccount);
                    recipientTransaction.setTransactionType(TransactionType.DEPOSIT);
                    recipientTransaction.setAmount(recipientAmount);
                    recipientTransaction.setTransactionDate(LocalDateTime.now());
                    recipientTransaction.setRecipientAccount(senderAccount);
                    context.getTransactionService().saveTransaction(recipientTransaction);
                }

                sendMessage(context, "Transaction is successful.");
            } catch (NumberFormatException e) {
                sendMessage(context, "Invalid amount format. Please, enter a valid number (e.g., 100.50).");
            }
        }

        @Override
        public BotState nextState() {
            return Menu;
        }
    },


    EnterPassword {
        private BotState next;
        private int counterOfInputProblem = 0;

            @Override
            public void enter(BotContext context) {
                sendMessage(context, "Enter your password please:");
            }

            @Override
            public void handleInput(BotContext context) {
                String password = context.getInput();
                int messageId = context.getMessageId();

                if (Utils.isValidPassword(password)) {
                    String encodedPassword = Utils.encodePassword(password);
                    context.getUser().setPassword(encodedPassword);
                    sendMessage(context, "Password saved. Next state is Menu.");
                    deleteMessage(context, messageId);
                    next = Menu;
                } else {
                    sendMessage(context, "Invalid password format!");
                    counterOfInputProblem++;
                    if (counterOfInputProblem > 3) {
                        sendMessage(context, "Input problem, return to start.");
                        deleteMessage(context, messageId);
                        next = Start;
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

    EnterPasswordForTransaction {
        private BotState next;
        private int counterOfInputProblem = 0;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Please, enter your password for transaction:");
        }

        @Override
        public void handleInput(BotContext context) {
            String password = context.getInput();
            int messageId = context.getMessageId();

            User user = context.getUser();
            String encodedPassword = user.getPassword();

            if (Utils.matchesPassword(password, encodedPassword)) {
                sendMessage(context, "Password verified.");
                deleteMessage(context, messageId);
                next = EnterAmountForTransactionAndMakeTransaction;
            } else {
                sendMessage(context, "Invalid password format or wrong password");
                counterOfInputProblem++;
                if (counterOfInputProblem > 3) {
                    sendMessage(context, "Input problem, return to Menu.");
                    deleteMessage(context, messageId);
                    next = Menu;
                } else {
                    sendMessage(context, "Enter your password again please:");
                }
            }
        }

        @Override
        public BotState nextState() {
            return next;
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
}