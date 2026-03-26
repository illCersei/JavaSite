package cersei.auth.service;

import cersei.auth.dto.*;
import cersei.auth.model.User;

import java.util.UUID;

public interface AuthService {
    User register(UserRegisterDto userRegisterDto);
    LoginOkResponseDto login(UserLoginDto userLoginDto);

    LoginOkResponseDto refresh(RefreshTokenDto dto);
    void updateAuthData(UUID id, AuthDataChangeDto dto);
}