package s21.di;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import s21.datasource.repository.GameRepository;
import s21.datasource.repository.RefreshTokenRepository;
import s21.datasource.repository.UserRepository;
import s21.di.infrastructure.EventNotifier;
import s21.domain.service.*;
import s21.security.service.AuthorizationService;
import s21.security.service.AuthorizationServiceImplementation;
import s21.security.util.JwtProvider;
import s21.security.util.JwtUtil;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
@EnableWebMvc
@ComponentScan("s21")
public class SpringConfig implements WebMvcConfigurer {
    private final ApplicationContext applicationContext;

    @Autowired
    public SpringConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    EventNotifier eventNotifier(GameRepository gameRepository) {
        return new EventNotifier(gameRepository);
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Bean
    public GameService gameService(GameRepository gameRepository, EventNotifier eventNotifier) {
        return new GameServiceImplementation(gameRepository, eventNotifier);
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImplementation(userRepository);
    }

    @Bean
    public AuthorizationService authorizationService(UserService userService, JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository) {
        return new AuthorizationServiceImplementation(userService, jwtProvider, refreshTokenRepository);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/favicon.ico");
    }

    @Bean
    public SecretKey jwtSecretKey(Environment environment) {
        String secret = environment.getProperty("jwt-authorization.secret");
        return Keys.hmacShaKeyFor(Objects.requireNonNull(secret).getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtProvider jwtProvider(SecretKey jwtSecretKey, RefreshTokenRepository refreshTokenRepository) {
        return new JwtProvider(jwtSecretKey, refreshTokenRepository);
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        registry.viewResolver(resolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/js/");
    }
}
