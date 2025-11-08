package cersei.auth.controller;

import cersei.auth.jwt.JWTGenerator;
import cersei.auth.model.User;
import cersei.auth.service.UserService;
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
    private final UserService userService;
    private final JWTGenerator jwtGenerator;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.saveUser(user);
        return ResponseEntity.ok("Регистрация прошла успешно");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User requestUser) {
        if (requestUser.getUsername() == null || requestUser.getPasswordHash() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username or password is empty"));
        }

        try {
            User userData = userService.getByUsername(requestUser.getUsername());
            PasswordEncoder encoder = new BCryptPasswordEncoder();

            if (!encoder.matches(requestUser.getPasswordHash(), userData.getPasswordHash())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password"));
            }

            Map<String, String> token = jwtGenerator.generateToken(userData);
            return ResponseEntity.ok(token);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
