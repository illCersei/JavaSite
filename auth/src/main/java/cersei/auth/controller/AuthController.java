package cersei.auth.controller;

import cersei.auth.dto.*;
import cersei.auth.service.AuthService;
import cersei.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/public/register")
    public ResponseEntity<RegisterOkDto> register(@RequestBody @Valid UserRegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.ok(new RegisterOkDto());
    }

    @PostMapping("/public/login")
    public ResponseEntity<LoginOkResponseDto> login(@RequestBody @Valid UserLoginDto dto) {
        LoginOkResponseDto response = authService.login(dto);
        String refreshToken = response.getRefreshToken();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "refreshToken=" + refreshToken + "; HttpOnly; Secure; Path=/; Max-Age=" + (7 * 24 * 60 * 60))
                .body(response);
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<LoginOkResponseDto> refresh(@CookieValue("refreshToken") String token) {
        RefreshTokenDto dto = new RefreshTokenDto(token);
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @PostMapping("/public/logout")
    public ResponseEntity<String> logout(@CookieValue("refreshToken") String token) {
        RefreshTokenDto dto = new RefreshTokenDto(token);
        refreshTokenService.delete(dto.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "refreshToken=; HttpOnly; Secure; Path=/; Max-Age=0")
                .body("Loggedout");
    }

    @PatchMapping("/private/update")
    public ResponseEntity<String> update(@AuthenticationPrincipal Jwt jwt,
                                         @RequestBody AuthDataChangeDto dto,
                                         @CookieValue("refreshToken") String token)
    {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        authService.updateAuthData(userId, dto);

        RefreshTokenDto tokenDto = new RefreshTokenDto(token);
        refreshTokenService.delete(tokenDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "refreshToken=; HttpOnly; Secure; Path=/; Max-Age=0")
                .body("Loggedout");
    }
}