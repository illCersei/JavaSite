package cersei.auth.unit.AuthServiceImpl;

import cersei.auth.dto.LoginOkResponseDto;
import cersei.auth.dto.UserLoginDto;
import cersei.auth.exception.AuthException;
import cersei.auth.jwt.JWTGeneratorImpl;
import cersei.auth.messaging.RabbitAuthMessagingService;
import cersei.auth.service.RefreshTokenService;
import cersei.auth.model.User;
import cersei.auth.repository.UserRepository;
import cersei.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginTest {
    final String USERNAME = "testAccount";
    final String PASSWORD = "GoodPass";

    private User userExisted;
    private UserLoginDto userWhoLoggingIn;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTGeneratorImpl jwtGenerator;

    @Mock
    private RabbitAuthMessagingService rabbitAuthMessagingService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    AuthServiceImpl authService;

    @BeforeEach
    void setup(){
        userWhoLoggingIn = new UserLoginDto();
        userWhoLoggingIn.setUsername(USERNAME);
        userWhoLoggingIn.setPassword(PASSWORD);

        userExisted = new User();
        userExisted.setUsername(USERNAME);
        userExisted.setPassword("HashesPassword");
        userExisted.setEmail("Goodmail@mail.ru");
        userExisted.setRole("USER");
    }

    @Test
    void login_WithValidUsernameAndPassword_SuccessLoginRabbitSentLoginName(){
        when(userRepository.findByUsername(userWhoLoggingIn.getUsername())).thenReturn(Optional.of(userExisted));
        when(passwordEncoder.matches(userWhoLoggingIn.getPassword(), userExisted.getPassword())).thenReturn(true);
        when(jwtGenerator.generateToken(userExisted)).thenReturn("AccessToken");
        when(refreshTokenService.create(userExisted.getUserId())).thenReturn("RefreshToken");

        LoginOkResponseDto login = authService.login(userWhoLoggingIn);

        assertEquals("Login success", login.getMessage());
        assertEquals(userExisted.getUsername(), login.getUsername());
        assertEquals(userExisted.getEmail(), login.getEmail());
        assertEquals("AccessToken", login.getToken());
        assertEquals("RefreshToken", login.getRefreshToken());

        verify(rabbitAuthMessagingService, times(1)).successLogin(anyString());
        verify(rabbitAuthMessagingService, never()).failureLogin(anyString());
        verify(refreshTokenService).create(userExisted.getUserId());
    }

    @Test
    void login_WithNotValidUsername_UnsuccessLoginRabbitSentMessageWithAnError(){
        when(userRepository.findByUsername(userWhoLoggingIn.getUsername())).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class, () -> authService.login(userWhoLoggingIn));

        assertEquals(AuthException.class, ex.getClass());
        assertEquals("Неверные данные для логина", ex.getMessage());

        verify(rabbitAuthMessagingService, never()).successLogin(anyString());
        verify(rabbitAuthMessagingService, times(1)).failureLogin(anyString());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtGenerator, never()).generateToken(any());
        verify(refreshTokenService, never()).create(any());
    }

    @Test
    void login_WithNotValidPassword_UnsuccessLoginRabbitSentMessageWithAnError(){
        when(userRepository.findByUsername(userWhoLoggingIn.getUsername())).thenReturn(Optional.of(userExisted));
        when(passwordEncoder.matches(userWhoLoggingIn.getPassword(), userExisted.getPassword()))
                .thenReturn(false);

        AuthException ex = assertThrows(AuthException.class, () -> authService.login(userWhoLoggingIn));

        assertEquals(AuthException.class, ex.getClass());
        assertEquals("Неверные данные для логина", ex.getMessage());

        verify(rabbitAuthMessagingService, never()).successLogin(anyString());
        verify(rabbitAuthMessagingService, times(1)).failureLogin(anyString());
        verify(passwordEncoder, times(1)).matches(any(), any());
        verify(jwtGenerator, never()).generateToken(any());
        verify(refreshTokenService, never()).create(any());
    }
}
