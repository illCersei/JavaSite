package cersei.auth.service;

import cersei.auth.dto.LoginOkResponseDto;
import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;

public interface AuthService {
    void register(UserRegisterDto userRegisterDto);
    LoginOkResponseDto login(UserLoginDto userLoginDto);
}