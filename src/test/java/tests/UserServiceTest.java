package tests;

import botBank.bot.BotContext;
import botBank.bot.ChatBot;
import botBank.model.User;
import botBank.repo.UserRepository;
import botBank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BotContext context;

    @Mock
    private ChatBot bot;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getBot()).thenReturn(bot);
    }

    @Test
    void testFindByTelegramId() {
        Long telegramId = 123456789L;
        User user = new User();
        when(userRepository.findByTelegramId(telegramId)).thenReturn(user);

        User foundUser = userService.findByTelegramId(telegramId);

        assertNotNull(foundUser);
        verify(userRepository, times(1)).findByTelegramId(telegramId);
    }

    @Test
    void testFindByNumber() {
        String number = "123-456-7890";
        User user = new User();
        when(userRepository.findByNumber(number)).thenReturn(user);

        User foundUser = userService.findByNumber(number);

        assertNotNull(foundUser);
        verify(userRepository, times(1)).findByNumber(number);
    }

    @Test
    void testAddUser() {
        User user = new User();

        userService.addUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser() {
        User user = new User();

        userService.updateUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindAllUsers() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> foundUsers = userService.findAllUsers();

        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testListUsers() throws TelegramApiException {
        User user1 = new User();
        user1.setTelegramId(123L);
        user1.setNumber("123-456-7890");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setTelegramId(456L);
        user2.setNumber("456-789-0123");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);
        when(context.getUser()).thenReturn(user1);

        userService.listUsers(context);


        verify(bot, times(1)).execute(any(SendMessage.class));
        verify(context, times(1)).getBot();
    }
}
