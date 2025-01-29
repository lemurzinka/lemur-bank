package botBank.service;

import botBank.bot.BotContext;
import botBank.model.Account;
import botBank.model.Card;
import botBank.model.CardType;
import botBank.model.User;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class CardAccountService {

    private static final Logger LOGGER = LogManager.getLogger(CardAccountService.class);

    private final CardService cardService;
    private final AccountService accountService;



    public void createCreditCardAndAccount(User user, BigDecimal initialBalance, String currency, BotContext context) {
        LOGGER.info("Creating credit card and account for user: {}, initial balance: {}, currency: {}", user.getId(), initialBalance, currency);

        Account account = Account.builder()
                .accountNumber(accountService.generateAccountNumber())
                .user(user)
                .creditBalance(initialBalance)
                .currentBalance(initialBalance)
                .currency(currency)
                .build();

        accountService.verifyAndSaveAccount(account);

        Card card = Card.builder()
                .expirationDate(cardService.generateExpirationDate())
                .cvv(cardService.generateCVV())
                .cardNumber(cardService.generateCardNumber(CardType.CREDIT))
                .cardType(CardType.CREDIT)
                .user(user)
                .account(account)
                .build();

        cardService.addCard(card);
        sendMessage(context, "You created a credit card. You can see more info in menu (my cards).");
    }



    public void createDebitCardAndAccount(User user, String currency, BotContext context) {
        LOGGER.info("Creating debit card and account for user: {}, currency: {}", user.getId(), currency);

        Account account = Account.builder()
                .accountNumber(accountService.generateAccountNumber())
                .user(user)
                .currentBalance(BigDecimal.ZERO)
                .currency(currency)
                .build();

        accountService.verifyAndSaveAccount(account);

        Card card = Card.builder()
                .expirationDate(cardService.generateExpirationDate())
                .cvv(cardService.generateCVV())
                .cardNumber(cardService.generateCardNumber(CardType.DEBIT))
                .cardType(CardType.DEBIT)
                .user(user)
                .account(account)
                .build();

        cardService.addCard(card);
        sendMessage(context, "You created a debit card. You can see more info in menu (my cards).");
    }


    private static void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getTelegramId()));
        message.setText(text);

        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send message to chat ID: {}", context.getUser().getTelegramId(), e);
        }
    }
}
