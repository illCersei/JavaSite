package cersei.auth.jwt;

import cersei.auth.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTGeneratorImpl implements JWTGenerator {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${app.jwttoken.message}")
    private String message;

    @Override
    public Map<String, String> generateToken(User user) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String jwtToken = Jwts.builder()
                .claims(Map.of(
                        "sub", user.getUsername(),
                        "role", user.getRole()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", jwtToken);
        tokenMap.put("message", message);
        return tokenMap;
    }

}
