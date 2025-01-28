package botBank.service;

import botBank.bot.BotContext;
import botBank.bot.BotState;
import botBank.model.Card;
import botBank.model.User;
import botBank.model.TransactionDetail;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CommandService {

    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final CurrencyRateService rateService;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final MessageService messageService;

    public CommandService(UserService userService, CardService cardService, CardAccountService cardAccountService, CurrencyRateService rateService, TransactionService transactionService, AccountService accountService, MessageService messageService) {
        this.userService = userService;
        this.cardService = cardService;
        this.cardAccountService = cardAccountService;
        this.rateService = rateService;
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.messageService = messageService;
    }

    public void handleCommand(String callbackData, BotContext context) {
        User user = context.getUser();
        long chatId = context.getUser().getTelegramId();

        switch (callbackData) {
            case "/update":
                user.setStateId(BotState.EnterPasswordForUpdate.ordinal());
                messageService.sendMessage(chatId, "You selected to update.");
                break;
            case "/addcard":
                user.setStateId(BotState.AddCard.ordinal());
                messageService.sendMessage(chatId, "You selected to add a card.");
                break;
            case "/listusers":
                userService.listUsers(context);
                break;
            case "/banuser":
                user.setStateId(BotState.BanUser.ordinal());
                messageService.sendMessage(chatId, "You selected to ban user.");
                break;
            case "/unbanuser":
                user.setStateId(BotState.UnbanUser.ordinal());
                messageService.sendMessage(chatId, "You selected to unban user.");
                break;
            case "credit":
                context.getCardAccountService().createCreditCardAndAccount(user, BigDecimal.valueOf(5000), "UAH", context);
                user.setStateId(BotState.Menu.ordinal());
                break;
            case "debit":
                messageService.sendMessage(chatId, "You selected to add a debit card.");
                user.setStateId(BotState.ChoseCurrency.ordinal());
                break;
            case "UAH":
            case "USD":
            case "EUR":
                context.getCardAccountService().createDebitCardAndAccount(user, callbackData, context);
                user.setStateId(BotState.Menu.ordinal());
                break;
            case "/mycards":
                List<Card> userCards = cardService.getCardsByUserId(user.getId());
                if (userCards.isEmpty()) {
                    messageService.sendMessage(chatId, "No cards now");
                } else {
                    messageService.sendMessage(chatId, cardService.formatCardDetails(userCards));
                }
                break;

            case "/rates":
                messageService.sendMessage(chatId, rateService.getFormattedRates());
                break;
            case "/send":
                TransactionDetail transactionDetail = new TransactionDetail();
                user.setTransactionDetail(transactionDetail);
                user.setStateId(BotState.EnterCardNumberForTransaction.ordinal());
                break;
            case "/hi":
                user.setStateId(BotState.EnterPhone.ordinal());
                break;
            case "/listcards":
                cardService.listCards(context);
                break;
            case "/listaccounts":
                accountService.listAccounts(context);
                break;
            case "/bancard":
                user.setStateId(BotState.BanCard.ordinal());
                messageService.sendMessage(chatId, "You selected to ban card.");
                break;
            case "/unbancard":
                user.setStateId(BotState.UnbanCard.ordinal());
                messageService.sendMessage(chatId, "You selected to unban card.");
                break;

            default:
                messageService.sendMessage(chatId, "Invalid option.");
                break;
        }

        userService.updateUser(user);
        BotState state = BotState.byId(user.getStateId());
        state.enter(context);
    }
}
