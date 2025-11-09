package cersei.auth.controller;

import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;
import cersei.auth.jwt.JWTGenerator;
import cersei.auth.model.User;
import cersei.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JWTGenerator jwtGenerator;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterDto userRegisterDto) {
        boolean created = authService.register(userRegisterDto);
        if (!created) {
            return ResponseEntity.badRequest().body("Пользователь с таким именем уже существует");
        }
        return ResponseEntity.ok("Регистрация прошла успешно");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto userLoginDto) {
        Map<String, String> token = authService.login(userLoginDto);
        return ResponseEntity.ok(token);
    }

}
