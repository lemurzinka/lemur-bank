package tests;

import bot_bank.bot.BotContext;
import bot_bank.bot.ChatBot;
import bot_bank.model.User;
import bot_bank.service.UserListingService;
import bot_bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserListingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private BotContext botContext;

    @Mock
    private ChatBot bot;

    @Mock
    private User user;

    @InjectMocks
    private UserListingService userListingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(botContext.getUser()).thenReturn(user);
        when(botContext.getBot()).thenReturn(bot);
        when(user.getTelegramId()).thenReturn(123456789L);
    }

    @Test
    void testListUsers_WithUsers() throws TelegramApiException {
        User user1 = new User();
        user1.setTelegramId(12345L);
        user1.setNumber("111-111-1111");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setTelegramId(67890L);
        user2.setNumber("222-222-2222");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(user1, user2);
        when(userService.findAllUsers()).thenReturn(users);

        userListingService.listUsers(botContext);

        verify(userService, times(1)).findAllUsers();
        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testListUsers_NoUsers() throws TelegramApiException {
        List<User> users = Collections.emptyList();
        when(userService.findAllUsers()).thenReturn(users);

        userListingService.listUsers(botContext);

        verify(userService, times(1)).findAllUsers();
        verify(bot, times(1)).execute(any(SendMessage.class));
    }
}
