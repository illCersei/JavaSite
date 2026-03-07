package cersei.auth.unit;

import cersei.auth.dto.LoginOkResponseDto;
import cersei.auth.dto.UserLoginDto;
import cersei.auth.jwt.JWTGeneratorImpl;
import cersei.auth.messaging.RabbitAuthMessagingService;
import cersei.auth.model.User;
import cersei.auth.repository.UserRepository;
import cersei.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTGeneratorImpl jwtGenerator;

    @Mock
    private RabbitAuthMessagingService rabbitAuthMessagingService;

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
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail("Goodmail@mail.ru");
        user.setRole("USER");
    }

    @Test
    void SuccessLoginWithGoodCreds(){
        when(userRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.of(user));
        when(jwtGenerator.generateToken(user)).thenReturn("GoodToken");

        LoginOkResponseDto login = authService.login(userLoginDto);

        assertEquals("Login success", login.getMessage());
        assertEquals("GoodToken", login.getToken());

        verify(rabbitAuthMessagingService, times(1)).successLogin(anyString());
        verify(rabbitAuthMessagingService, times(0)).failureLogin(anyString());
    }
}
