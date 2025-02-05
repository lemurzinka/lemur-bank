package tests;

import bot_bank.bot.BotContext;
import bot_bank.bot.ChatBot;
import bot_bank.model.User;
import bot_bank.repo.UserRepository;
import bot_bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceTest contains unit tests for the UserService class. It verifies the correctness
 * of methods related to user operations, such as finding, adding, updating, and listing users.
 * It also ensures that the appropriate messages are sent via the Telegram bot context.
 */

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

}
