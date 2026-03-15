package cersei.auth.controller;

import cersei.auth.dto.*;
import cersei.auth.service.AuthService;
import cersei.auth.service.RefreshTokenService;
import cersei.common.error.errors.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Методы")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private  final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создаёт нового пользователя на основе переданных данных.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Регистрация прошла успешно",
                            content = @Content(schema = @Schema(implementation = RegisterOkDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации / некорректное тело запроса",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Пользователь с таким именем уже существует",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterOkDto> register(@RequestBody @Valid UserRegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.ok(new RegisterOkDto());
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Принимает логин и пароль, выполняет авторизацию и возвращает JWT-токен.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для входа пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserLoginDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная авторизация",
                            content = @Content(schema = @Schema(implementation = LoginOkResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации / некорректное тело запроса",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неверные учетные данные",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginOkResponseDto> login(@RequestBody @Valid UserLoginDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginOkResponseDto> refresh(@RequestBody RefreshTokenDto dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenDto dto) {
        refreshTokenService.delete(dto.getRefreshToken());
        return ResponseEntity.ok("Logouted, токен удален");
    }
}