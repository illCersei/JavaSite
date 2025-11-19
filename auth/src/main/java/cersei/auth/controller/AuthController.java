package cersei.auth.controller;

import cersei.auth.dto.*;
import cersei.auth.error.ApiLoginErrorResponse;
import cersei.auth.service.AuthService;
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

import java.util.Map;

@Tag(name = "Методы")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создаёт нового пользователя на основе переданных данных.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации пользователя",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserRegisterDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Регистрация прошла успешно",
                            content = @Content(
                                    schema = @Schema(implementation = RegisterOkDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Пользователь с таким именем уже существует",
                            content = @Content(
                                    schema = @Schema(implementation = RegisterBadDto.class)
                            )
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegisterDto userRegisterDto) {
        boolean created = authService.register(userRegisterDto);
        if (!created) {
            return ResponseEntity.badRequest().body(new RegisterBadDto());
        }
        return ResponseEntity.ok(new RegisterOkDto());
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Принимает логин и пароль, выполняет авторизацию и возвращает JWT-токен.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для входа пользователя",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserLoginDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная авторизация",
                            content = @Content(
                                    schema = @Schema(implementation = LoginOkResponseDto.class)
                            )),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные данные для логина",
                            content = @Content(
                                    schema = @Schema(implementation = ApiLoginErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginOkResponseDto> login(@RequestBody @Valid UserLoginDto userLoginDto) {
        Map<String, String> token = authService.login(userLoginDto);
        return ResponseEntity.ok(new LoginOkResponseDto(token));
    }

}
