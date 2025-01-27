package botBank.event;

import botBank.bot.BotContext;
import org.springframework.context.ApplicationEvent;

public class BotEvent extends ApplicationEvent {
    private final BotContext context;

    public BotEvent(Object source, BotContext context) {
        super(source);
        this.context = context;
    }

     BotContext getContext() {
        return context;
    }
}
