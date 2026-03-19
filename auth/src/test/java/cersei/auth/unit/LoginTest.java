package cersei.auth.unit;

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

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginTest {

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
    private User user;
    private UserLoginDto userLoginDto;

    @BeforeEach
    void setup(){
        String USERNAME = "testAccount";
        String PASSWORD = "GoodPass";

        userLoginDto = new UserLoginDto();
        userLoginDto.setUsername(USERNAME);
        userLoginDto.setPassword(PASSWORD);

        user = new User();
        user.setUsername(USERNAME);
        user.setPassword("HashesPassword");
        user.setEmail("Goodmail@mail.ru");
        user.setRole("USER");
    }

    @Test
    void SuccessLoginWithGoodCreds(){
        when(userRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtGenerator.generateToken(user)).thenReturn("GoodToken");

        LoginOkResponseDto login = authService.login(userLoginDto);

        assertEquals("Login success", login.getMessage());
        assertEquals("GoodToken", login.getToken());

        verify(rabbitAuthMessagingService, times(1)).successLogin(anyString());
        verify(rabbitAuthMessagingService, times(0)).failureLogin(anyString());
    }

    @Test
    void UnsuccessLoginWithBadUsername(){
        when(userRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class, () -> authService.login(userLoginDto));

        assertEquals(AuthException.class, ex.getClass());
        assertEquals("Неверные данные для логина", ex.getMessage());

        verify(rabbitAuthMessagingService, times(0)).successLogin(anyString());
        verify(rabbitAuthMessagingService, times(1)).failureLogin(anyString());
    }

    @Test
    void UnsuccessLoginWithBadPassword(){
        when(userRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword()))
                .thenReturn(false);

        AuthException ex = assertThrows(AuthException.class, () -> authService.login(userLoginDto));

        assertEquals(AuthException.class, ex.getClass());
        assertEquals("Неверные данные для логина", ex.getMessage());

        verify(rabbitAuthMessagingService, times(0)).successLogin(anyString());
        verify(rabbitAuthMessagingService, times(1)).failureLogin(anyString());
    }
}
