package tests;

import botBank.service.BotExecutor;
import botBank.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * MessageServiceTest contains unit tests for the MessageService class. It verifies the correctness
 * of methods related to sending messages via the Telegram bot and handles exceptions that may occur
 * during the message sending process.
 */

class MessageServiceTest {

    @Mock
    private BotExecutor botExecutor;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageService = new MessageService(botExecutor);
    }

    @Test
    void testSendMessage() throws TelegramApiException {
        long chatId = 123456789L;
        String text = "Hello, world!";
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(chatId));
        message.setText(text);

        messageService.sendMessage(chatId, text);

        verify(botExecutor, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendMessageExceptionHandling() throws TelegramApiException {
        long chatId = 123456789L;
        String text = "Hello, world!";
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(chatId));
        message.setText(text);

        doThrow(TelegramApiException.class).when(botExecutor).execute(any(SendMessage.class));

        messageService.sendMessage(chatId, text);

        verify(botExecutor, times(1)).execute(any(SendMessage.class));
    }
}
