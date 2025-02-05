package bot_bank.service;

import bot_bank.bot.BotContext;
import bot_bank.model.Card;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static bot_bank.service.UserListingService.sendMessage;


/**
 * CardServiceFacade provides a facade for managing card-related operations, delegating the actual
 * operations to the CardService.
 */
@Service
@RequiredArgsConstructor
public class CardServiceFacade {

    private static final Logger LOGGER = LogManager.getLogger(CardServiceFacade.class);

    private final CardService cardService;

    /**
     * Lists all cards and sends the card details to the user via the Telegram bot.
     *
     * @param context the bot context containing user and bot information
     */
    public void listCards(BotContext context) {
        StringBuffer sb = new StringBuffer("All cards list:\r\n");
        List<Card> cards = cardService.findAllCards();

        if (cards.isEmpty()) {
            sb.append("No cards found.");
        } else {
            cards.forEach(card -> sb.append(card.getCardNumber())
                    .append(" ")
                    .append(card.getExpirationDate())
                    .append(" ")
                    .append(card.getCardType())
                    .append("\r\n"));
        }

        LOGGER.info("Listing all cards");
        sendMessage(context, sb.toString());
    }
}
