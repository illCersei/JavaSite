package cersei.auth.service;

import cersei.auth.dto.*;
import cersei.auth.messaging.KafkaProducerService;
import cersei.auth.exception.AuthException;
import cersei.auth.jwt.JWTGeneratorImpl;
import cersei.auth.messaging.RabbitAuthMessagingService;
import cersei.auth.model.RefreshToken;
import cersei.auth.model.User;
import cersei.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGeneratorImpl jwtGenerator;
    private final RabbitAuthMessagingService rabbitAuthMessagingService;
    private final RefreshTokenService refreshTokenService;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
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

        kafkaProducerService.sendUserRegistrationEvent(user);

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

        return new LoginOkResponseDto("Login success", user.getUsername(), user.getEmail(), accessToken, refreshToken);
    }

    @Override
    public LoginOkResponseDto refresh(RefreshTokenDto dto) {
        RefreshToken refreshToken = refreshTokenService.validate(dto.getRefreshToken());

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new AuthException("Пользователь не найден", HttpStatus.UNAUTHORIZED));

        String accessToken = jwtGenerator.generateToken(user);
        String rotatedRefreshToken = refreshTokenService.rotate(refreshToken);

        return new LoginOkResponseDto("Token refreshed", user.getUsername(), user.getEmail(), accessToken, rotatedRefreshToken);
    }

    @Override
    public void updateAuthData(UUID userId, AuthDataChangeDto dto) {
        log.info("Обновляем юзера {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }
}