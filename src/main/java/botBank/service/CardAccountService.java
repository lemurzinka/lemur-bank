package botBank.service;

import botBank.bot.BotContext;
import botBank.model.Account;
import botBank.model.Card;
import botBank.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

@Service
public class CardAccountService {

    private final CardService cardService;
    private final AccountService accountService;





    @Autowired
    public CardAccountService(CardService cardService, AccountService accountService) {
        this.cardService = cardService;
        this.accountService = accountService;
    }

    public void createCreditCardAndAccount(User user, BigDecimal initialBalance, String currency, BotContext context) {
        Card card = cardService.createCard("CREDIT");
        Account account = accountService.createAccount(user);
        account.setBalance(initialBalance);
        account.setCurrentBalance(initialBalance);
        account.setCurrency(currency);
        accountService.verifyAndSaveAccount(account);

        card.setUser(user);
        card.setAccount(account);
        cardService.addCard(card);
        sendMessage(context, "You created a credit card. You can see more info in menu (my cards).");
    }

    public void createDebitCardAndAccount(User user, String currency, BotContext context) {
        Card card = cardService.createCard("DEBIT");
        Account account = accountService.createAccount(user);
        account.setCurrentBalance(BigDecimal.ZERO);
        account.setCurrency(currency);
        accountService.verifyAndSaveAccount(account);

        card.setUser(user);
        card.setAccount(account);
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
            e.printStackTrace();
        }

    };
}
