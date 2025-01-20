package botBank.event;

import botBank.service.CommandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
