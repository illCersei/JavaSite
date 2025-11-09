package cersei.auth.service;

import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;
import cersei.auth.model.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

public interface AuthService {
    public boolean register(UserRegisterDto userregisterDto);
    Map<String, String> login(UserLoginDto userloginDto);
}
