package cersei.auth.service;

import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;

import java.util.Map;

public interface AuthService {
    boolean register(UserRegisterDto userregisterDto);
    Map<String, String> login(UserLoginDto userloginDto);
}
