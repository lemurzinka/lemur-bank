package bot_bank.service;

import bot_bank.bot.BotContext;
import bot_bank.model.Account;
import bot_bank.model.Card;
import bot_bank.model.Transaction;
import bot_bank.model.TransactionType;
import bot_bank.repo.TransactionRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * TransactionService manages the processing of financial transactions.
 * It provides methods to parse amounts, validate sender details, retrieve recipient accounts,
 * convert currencies, and save transactions. It also handles the processing of transactions
 * between accounts and sending messages to users via Telegram.
 */

@Service
@AllArgsConstructor
public class TransactionService {

    private static final Logger LOGGER = LogManager.getLogger(TransactionService.class);


    private final TransactionRepository transactionRepository;


    private final CurrencyRateService currencyRateService;


    private final AccountService accountService;


    private final CardService cardService;

    @Transactional
    public void saveTransaction(Transaction transaction) {
        LOGGER.info("Saving transaction: {}", transaction);
        transactionRepository.save(transaction);
    }


    public BigDecimal parseAmount(String amountStr, BotContext context) {
        try {
            BigDecimal senderAmount = new BigDecimal(amountStr);
            if (senderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                LOGGER.warn("Invalid amount entered: {}", senderAmount);
                sendMessage(context, "The amount must be greater than zero. Please, try again.");
                return null;
            }
            context.getUser().getTransactionDetail().setAmount(senderAmount);
            LOGGER.info("Amount for transaction: {}", senderAmount);
            return senderAmount;
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid amount format: {}", amountStr, e);
            sendMessage(context, "Invalid amount format. Please, enter a valid number (e.g., 100.50).");
            return null;
        }
    }

    public boolean validateSenderDetails(BotContext context, BigDecimal senderAmount) {
        String senderCardNumber = context.getUser().getTransactionDetail().getSenderCardNumber();
        String senderCvv = context.getUser().getTransactionDetail().getSenderCvv();
        String senderExpDateStr = context.getUser().getTransactionDetail().getSenderExpDate();
        LocalDate senderExpDate;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            senderExpDate = LocalDate.parse(senderExpDateStr, formatter);
        } catch (DateTimeParseException e) {
            LOGGER.error("Invalid expiration date format: {}", senderExpDateStr, e);
            sendMessage(context, "Invalid expiration date format. Use yyyy-MM-dd.");
            return false;
        }

        Optional<Card> senderCardOpt = cardService.findByCardNumber(senderCardNumber);
        if (senderCardOpt.isEmpty() || !senderCardOpt.get().getCvv().equals(senderCvv)
                || !senderCardOpt.get().getExpirationDate().equals(senderExpDate)) {
            LOGGER.warn("Invalid or not found sender card details");
            sendMessage(context, "Sender card details are invalid or not found.");
            return false;
        }

        Card senderCard = senderCardOpt.get();
        if (senderCard.isBanned()) {
            LOGGER.warn("Sender card is banned: {}", senderCardNumber);
            sendMessage(context, "Transaction cannot be processed because the sender's card is banned.");
            return false;
        }

        Account senderAccount = senderCard.getAccount();
        if (senderAccount.getCurrentBalance().compareTo(senderAmount) < 0) {
            LOGGER.warn("Insufficient funds on sender's account: {}", senderAccount.getCurrentBalance());
            sendMessage(context, "Insufficient funds on sender's account.");
            return false;
        }

        return true;
    }

    public Account getRecipientAccount(BotContext context) {
        String recipientCardNumber = context.getUser().getTransactionDetail().getRecipientCardNumber();
        Optional<Card> recipientCardOpt = cardService.findByCardNumber(recipientCardNumber);
        return recipientCardOpt.map(Card::getAccount).orElse(null);
    }

    public BigDecimal convertCurrencyIfNeeded(Account senderAccount, Account recipientAccount, BigDecimal senderAmount, BotContext context) {
        BigDecimal recipientAmount = senderAmount;
        String senderCurrency = senderAccount.getCurrency();
        String recipientCurrency = recipientAccount != null ? recipientAccount.getCurrency() : null;

        if (recipientAccount != null && !senderCurrency.equals(recipientCurrency)) {
            Double rate = currencyRateService.getRate(senderCurrency, recipientCurrency);
            if (rate != null) {
                recipientAmount = senderAmount.multiply(BigDecimal.valueOf(rate));
            } else {
                LOGGER.error("Currency conversion rate unavailable");
                sendMessage(context, "Currency conversion rate is unavailable.");
                return null;
            }
        }

        return recipientAmount;
    }

    @Transactional
    public void processTransaction(Account senderAccount, Account recipientAccount, BigDecimal senderAmount,
                                   BigDecimal recipientAmount, BotContext context) {
        senderAccount.setCurrentBalance(senderAccount.getCurrentBalance().subtract(senderAmount));
        accountService.saveAccount(senderAccount);
        LOGGER.info("Sender account balance updated: {}", senderAccount.getCurrentBalance());

        if (recipientAccount != null) {
            recipientAccount.setCurrentBalance(recipientAccount.getCurrentBalance().add(recipientAmount));
            accountService.saveAccount(recipientAccount);
            LOGGER.info("Recipient account balance updated: {}", recipientAccount.getCurrentBalance());
        }

        Transaction.TransactionBuilder senderTransactionBuilder = Transaction.builder()
                .account(senderAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(senderAmount.negate())
                .transactionDate(LocalDateTime.now());

        if (recipientAccount != null) {
            senderTransactionBuilder.recipientAccount(recipientAccount);
        } else {
            senderTransactionBuilder.recipientDetails("External recipient: " + context.getUser().getTransactionDetail().getRecipientCardNumber());
        }

        Transaction senderTransaction = senderTransactionBuilder.build();
        transactionRepository.save(senderTransaction);
        LOGGER.info("Sender transaction saved: {}", senderTransaction);

        if (recipientAccount != null) {
            Transaction recipientTransaction = Transaction.builder()
                    .account(recipientAccount)
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(recipientAmount)
                    .transactionDate(LocalDateTime.now())
                    .recipientAccount(senderAccount)
                    .build();

            transactionRepository.save(recipientTransaction);
            LOGGER.info("Recipient transaction saved: {}", recipientTransaction);
        }
    }


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
}
