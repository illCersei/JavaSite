package cersei.auth.service;

import cersei.auth.dto.LoginOkResponseDto;
import cersei.auth.dto.RefreshTokenDto;
import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;
import cersei.auth.exception.AuthException;
import cersei.auth.jwt.JWTGeneratorImpl;
import cersei.auth.messaging.RabbitAuthMessagingService;
import cersei.auth.model.RefreshToken;
import cersei.auth.model.User;
import cersei.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGeneratorImpl jwtGenerator;
    private final RabbitAuthMessagingService rabbitAuthMessagingService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public User register(UserRegisterDto userRegisterDto) {
        if (userRepository.existsByUsername(userRegisterDto.getUsername())) {
            throw new AuthException("Пользователь с таким логином существует", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(userRegisterDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.setEmail(userRegisterDto.getEmail());
        user.setRole("USER");

        userRepository.save(user);

        return user;
    }

    @Override
    public LoginOkResponseDto login(UserLoginDto userLoginDto) {

        User user = userRepository.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> {
                    rabbitAuthMessagingService.failureLogin("Неуспешный логин " + userLoginDto.getUsername());
                    return new AuthException("Неверные данные для логина", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            rabbitAuthMessagingService.failureLogin("Неуспешный логин " + userLoginDto.getUsername());
            throw new AuthException("Неверные данные для логина", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtGenerator.generateToken(user);
        String refreshToken = refreshTokenService.create(user.getUserId());

        rabbitAuthMessagingService.successLogin("Успешный логин " + userLoginDto.getUsername());

        return new LoginOkResponseDto("Login success", accessToken, refreshToken);
    }

    @Override
    public LoginOkResponseDto refresh(RefreshTokenDto dto) {
        RefreshToken refreshToken = refreshTokenService.validate(dto.getRefreshToken());

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new AuthException("Пользователь не найден", HttpStatus.UNAUTHORIZED));

        String accessToken = jwtGenerator.generateToken(user);

        return new LoginOkResponseDto("Token refreshed", accessToken, dto.getRefreshToken());
    }
}