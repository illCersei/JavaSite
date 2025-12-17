package cersei.newyearservice.config;

import cersei.error.CustomAccessDeniedHandler;
import cersei.error.CustomBearerTokenAuthenticationEntryPoint;
import cersei.jwt.JwtAuthenticationConverterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    CustomBearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint() {
        return new CustomBearerTokenAuthenticationEntryPoint();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverterConfig().jwtAuthConverter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomBearerTokenAuthenticationEntryPoint entryPoint,
                                           CustomAccessDeniedHandler accessHandler,
                                           JwtAuthenticationConverter jwtAuthConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessHandler))
                .oauth2ResourceServer(resourceServer ->
                        resourceServer
                                .authenticationEntryPoint(entryPoint)
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }
}

