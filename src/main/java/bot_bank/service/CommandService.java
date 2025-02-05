package bot_bank.service;

import bot_bank.bot.BotContext;
import bot_bank.bot.BotState;
import bot_bank.model.Card;
import bot_bank.model.User;
import bot_bank.model.TransactionDetail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * CommandService handles the processing of user commands received from the bot.
 * It updates the user's state and interacts with various services to perform actions
 * such as adding cards, banning users, listing accounts, and sending messages.
 */

@Service
@RequiredArgsConstructor
public class CommandService {

    private final UserService userService;
    private final CardService cardService;
    private final CardAccountService cardAccountService;
    private final CurrencyRateService rateService;
    private final TransactionService transactionService;
    private final AccountListingService accountListingService;
    private final MessageService messageService;
    private final CardServiceFacade cardServiceFacade;
    private final UserListingService userListingService;



    public void handleCommand(String callbackData, BotContext context) {
        User user = context.getUser();
        long chatId = context.getUser().getTelegramId();

        switch (callbackData) {
            case "/update":
                user.setStateId(BotState.ENTER_PASSWORD_FOR_UPDATE.ordinal());
                messageService.sendMessage(chatId, "You selected to update.");
                break;
            case "/addcard":
                user.setStateId(BotState.ADD_CARD.ordinal());
                messageService.sendMessage(chatId, "You selected to add a card.");
                break;
            case "/listusers":
                userListingService.listUsers(context);
                break;
            case "/banuser":
                user.setStateId(BotState.BAN_USER.ordinal());
                messageService.sendMessage(chatId, "You selected to ban user.");
                break;
            case "/unbanuser":
                user.setStateId(BotState.UNBAN_USER.ordinal());
                messageService.sendMessage(chatId, "You selected to unban user.");
                break;
            case "credit":
                context.getCardAccountService().createCreditCardAndAccount(user, BigDecimal.valueOf(5000), "UAH", context);
                user.setStateId(BotState.MENU.ordinal());
                break;
            case "debit":
                messageService.sendMessage(chatId, "You selected to add a debit card.");
                user.setStateId(BotState.CHOSE_CURRENCY.ordinal());
                break;
            case "UAH", "USD", "EUR":
                context.getCardAccountService().createDebitCardAndAccount(user, callbackData, context);
                user.setStateId(BotState.MENU.ordinal());
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
                user.setStateId(BotState.ENTER_CARD_NUMBER_FOR_TRANSACTION.ordinal());
                break;
            case "/hi":
                user.setStateId(BotState.ENTER_PHONE.ordinal());
                break;
            case "/listcards":
                cardServiceFacade.listCards(context);
                break;
            case "/listaccounts":
                accountListingService.listAccounts(context);
                break;
            case "/bancard":
                user.setStateId(BotState.BAN_CARD.ordinal());
                messageService.sendMessage(chatId, "You selected to ban card.");
                break;
            case "/unbancard":
                user.setStateId(BotState.UNBAN_CARD.ordinal());
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
