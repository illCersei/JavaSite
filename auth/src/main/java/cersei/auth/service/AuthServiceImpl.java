package cersei.auth.service;

import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;
import cersei.auth.exception.AuthException;
import cersei.auth.jwt.JWTGeneratorImpl;
import cersei.auth.model.User;
import cersei.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JWTGeneratorImpl jwtGenerator;

    @Override
    public boolean register(UserRegisterDto userRegisterDto) {
        if (userRepository.existsByUsername(userRegisterDto.getUsername())) {
            return false;
        }

        User user = new User();
        user.setUsername(userRegisterDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.setEmail(userRegisterDto.getEmail());
        user.setRole("USER");

        userRepository.save(user);
        return true;
    }

    @Override
    public Map<String, String> login(UserLoginDto userloginDto) {
        User user = userRepository.findByUsername(userloginDto.getUsername())
                .orElseThrow(() -> new AuthException("Неверные данные для логина", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(userloginDto.getPassword(), user.getPassword())) {
            throw new AuthException("Неверные данные для логина", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtGenerator.generateToken(user);
        return Map.of("token", token, "message", "Login success");
    }


}
