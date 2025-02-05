package bot_bank.event;

import bot_bank.service.CommandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * BotEventListener listens for BotEvent instances and handles them by invoking
 * the appropriate command on the CommandService.
 */
@Component
public class BotEventListener {
    private final CommandService commandService;

    public BotEventListener(CommandService commandService) {
        this.commandService = commandService;
    }

    @EventListener
    public void handleBotEvent(BotEvent event) {
        commandService.handleCommand(event.getContext().getInput(), event.getContext());
    }
}
