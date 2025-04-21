package s21.security.configuration;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import s21.security.authentication.CookieToHeaderTranslationFilter;
import s21.security.util.JwtProvider;
import s21.security.util.JwtUtil;
import s21.security.authentication.AuthFilter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
public class SecurityConfiguration {
    private final JwtProvider jwtProvider;
    private final JwtUtil jwtUtil;

    public SecurityConfiguration(JwtProvider jwtProvider, JwtUtil jwtUtil) {
        this.jwtProvider = jwtProvider;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/**", "/js/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterAfter(new AuthFilter(jwtProvider, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new CookieToHeaderTranslationFilter(), AuthFilter.class);
        return http.build();
    }
}
