package cersei.auth.unit;

import cersei.auth.dto.UserRegisterDto;
import cersei.auth.exception.AuthException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterTest {
    private final String PASSWORD = "GoodPass";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    AuthServiceImpl authService;

    private UserRegisterDto user;

    @BeforeEach
    void setup(){
        user = new UserRegisterDto();
        user.setUsername("testAccount");
        user.setPassword(PASSWORD);
        user.setEmail("testmail@mail.ru");
    }

    @Test
    void registerUserWithCorrectCredsAndFreeUsername(){
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.register(user);

        assertEquals(user.getUsername(), registered.getUsername());
        assertEquals(user.getEmail(), registered.getEmail());
        assertTrue(passwordEncoder.matches(PASSWORD, registered.getPassword()));
        assertEquals("USER", registered.getRole());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUserWithCorrectCredsAndNotFreeUsername(){
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        AuthException ex = assertThrows(AuthException.class, () -> authService.register(user));

        assertEquals(AuthException.class, ex.getClass());

        verify(userRepository, times(0)).save(any(User.class));
    }

}