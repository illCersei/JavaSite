package cersei.auth.service;

import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;
import cersei.auth.jwt.JWTGeneratorImpl;
import cersei.auth.model.User;
import cersei.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
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
        user.setRole(userRegisterDto.getRole());

        userRepository.save(user);
        return true;
    }

    @Override
    public Map<String, String> login(UserLoginDto userloginDto) {
        User user = userRepository.findByUsername(userloginDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Неверные данные для логина"));

        if (!passwordEncoder.matches(userloginDto.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Неверные данные для логина");
        }

        return jwtGenerator.generateToken(user);
    }


}
