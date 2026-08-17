package cersei.auth.service;

import cersei.auth.exception.AuthException;
import cersei.auth.model.RefreshToken;
import cersei.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public String create(UUID userId) {

        String token = UUID.randomUUID().toString() + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7));

        repository.save(refreshToken);

        return token;
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new AuthException("Refresh token не найден или недействителен", HttpStatus.UNAUTHORIZED));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new AuthException("Refresh token истёк", HttpStatus.UNAUTHORIZED);
        }

        return refreshToken;
    }

    /** Инвалидирует старый токен и выдаёт новый на то же имя пользователя (ротация при каждом refresh). */
    public String rotate(RefreshToken oldToken) {
        repository.delete(oldToken);
        return create(oldToken.getUserId());
    }

    public void delete(String token) {
        Optional<RefreshToken> rt = repository.findByToken(token);
        if (rt.isPresent()) {
            repository.delete(rt.get());
        } else {
            throw new AuthException("Refresh token не найден", HttpStatus.UNAUTHORIZED);
        }
    }
}