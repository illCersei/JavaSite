package cersei.auth.jwt;

import cersei.auth.model.User;

import java.util.Map;

public interface JWTGenerator {
    Map<String, String> generateToken(User user);
}
