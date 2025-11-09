package cersei.auth.jwt;

import cersei.auth.model.User;

public interface JWTGenerator {
    String generateToken(User user);
}
