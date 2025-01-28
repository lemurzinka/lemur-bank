package tests;

import botBank.bot.ChatBot;
import botBank.event.BotEvent;
import botBank.model.User;
import botBank.service.AccountService;
import botBank.service.CardAccountService;
import botBank.service.CardService;
import botBank.service.CurrencyRateService;
import botBank.service.TransactionService;
import botBank.service.UserService;
import botBank.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatBotTest {

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CardService cardService;

    @Mock
    private CardAccountService cardAccountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountService accountService;

    @Mock
    private CurrencyRateService currencyRateService;

    @Mock
    private ValidationService validationService;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private ChatBot chatBot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatBot = spy(new ChatBot(userService, eventPublisher, cardService, cardAccountService, transactionService, accountService, currencyRateService, validationService));
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executorService).submit(any(Runnable.class));
        ReflectionTestUtils.setField(chatBot, "executorService", executorService);
    }

    @Test
    void testOnUpdateReceived_withCallbackQuery() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("test_callback");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(123L);
        when(message.getMessageId()).thenReturn(1);
        User user = new User();
        user.setTelegramId(123L);
        user.setStateId(0);
        when(userService.findByTelegramId(123L)).thenReturn(user);

        chatBot.onUpdateReceived(update);

        verify(userService, times(1)).findByTelegramId(123L);
        verify(eventPublisher, times(1)).publishEvent(any(BotEvent.class));
    }

    @Test
    void testOnUpdateReceived_withMessage() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("test message");
        when(message.getChatId()).thenReturn(123L);
        when(message.getMessageId()).thenReturn(1);
        User user = new User();
        user.setTelegramId(123L);
        user.setStateId(0);
        when(userService.findByTelegramId(123L)).thenReturn(user);

        chatBot.onUpdateReceived(update);

        verify(userService, times(1)).findByTelegramId(123L);
        verify(userService, times(1)).updateUser(any(User.class));
    }



@Test
void testSendMessage() throws TelegramApiException {
    long chatId = 123L;
    String text = "Hello, world!";
    SendMessage message = new SendMessage();
    message.setChatId(Long.toString(chatId));
    message.setText(text);

    doNothing().when(chatBot).execute(any(SendMessage.class));
    chatBot.execute(message);

    verify(chatBot, times(1)).execute(any(SendMessage.class));
}

}
