package bot_bank.event;

import bot_bank.bot.BotContext;
import org.springframework.context.ApplicationEvent;

/**
 * BotEvent represents a custom application event that holds the bot context.
 * It is used to trigger specific actions based on the bot's current state and user input.
 */
public class BotEvent extends ApplicationEvent {
    private final transient BotContext context;

    public BotEvent(Object source, BotContext context) {
        super(source);
        this.context = context;
    }

    BotContext getContext() {
        return context;
    }
}
