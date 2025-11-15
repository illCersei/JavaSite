package cersei.testapi.config;

import cersei.error.CustomAccessDeniedHandler;
import cersei.error.CustomBearerTokenAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean CustomBearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint() {
        return new CustomBearerTokenAuthenticationEntryPoint();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomBearerTokenAuthenticationEntryPoint entryPoint,
                                           CustomAccessDeniedHandler accessHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessHandler))
                .oauth2ResourceServer(resourceServer->
                        resourceServer
                                .authenticationEntryPoint(entryPoint)
                                .jwt(jwt -> {}));

        return http.build();
    }

}
