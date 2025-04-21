package s21.security.authentication;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import s21.security.model.JwtAuthentication;
import s21.security.util.JwtProvider;
import s21.security.util.JwtUtil;

import java.io.IOException;

public class AuthFilter extends GenericFilterBean {
    private final JwtProvider jwtProvider;
    private final JwtUtil jwtUtil;

    public AuthFilter(JwtProvider jwtProvider, JwtUtil jwtUtil) {
        this.jwtProvider = jwtProvider;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();

        if (requestURI.equals("/") || requestURI.startsWith("/auth/") || requestURI.startsWith("/js/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect("/auth/login_error");
            return;
        }

        try {
            String token = authHeader.replace("Bearer ", "");

            if (!jwtProvider.validateAccessToken(token)) {
                throw new AuthenticationException("Invalid Access-token") {
                };
            }

            Claims claims = jwtProvider.getClaimsFromToken(token);
            JwtAuthentication jwtAuthentication = jwtUtil.createJwtAuthentication(claims);
            SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect("/auth/login_error");
        }
    }
}
