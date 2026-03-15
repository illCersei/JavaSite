package cersei.auth.service;

import cersei.auth.dto.LoginOkResponseDto;
import cersei.auth.dto.RefreshTokenDto;
import cersei.auth.dto.UserLoginDto;
import cersei.auth.dto.UserRegisterDto;
import cersei.auth.model.User;

public interface AuthService {
    User register(UserRegisterDto userRegisterDto);
    LoginOkResponseDto login(UserLoginDto userLoginDto);

    LoginOkResponseDto refresh(RefreshTokenDto dto);
}