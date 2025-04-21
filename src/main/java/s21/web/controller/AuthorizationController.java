package s21.web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s21.security.model.JwtRequest;
import s21.security.model.JwtResponse;
import s21.security.model.RefreshJwtRequest;
import s21.security.service.AuthorizationService;

import javax.security.auth.RefreshFailedException;

@Controller
@RequestMapping("/auth")
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/register")
    public String register(@ModelAttribute JwtRequest jwtRequest, RedirectAttributes redirectAttributes) {
        boolean status = authorizationService.registerUser(jwtRequest);
        redirectAttributes.addFlashAttribute("registration_status", status);
        return "redirect:/";
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authorize(@ModelAttribute JwtRequest jwtRequest, HttpServletResponse response) {
        try {
            JwtResponse jwtResponse = authorizationService.authorizeUser(jwtRequest);
            return getJwtResponseResponseEntity(response, jwtResponse);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/update_access_token")
    public ResponseEntity<JwtResponse> updateAccessToken(@ModelAttribute RefreshJwtRequest refreshJwtRequest,
                                                         HttpServletResponse response) {
        try {
            JwtResponse jwtResponse = authorizationService.updateAccessToken(refreshJwtRequest.getRefreshToken());
            return getJwtResponseResponseEntity(response, jwtResponse);
        } catch (RefreshFailedException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/update_refresh_token")
    public ResponseEntity<JwtResponse> updateRefreshToken(@ModelAttribute RefreshJwtRequest refreshJwtRequest,
                                                          HttpServletResponse response) {
        try {
            JwtResponse jwtResponse = authorizationService.updateRefreshToken(refreshJwtRequest.getRefreshToken());
            return getJwtResponseResponseEntity(response, jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/login_error")
    public String loginError() {
        return "login_error";
    }

    private ResponseEntity<JwtResponse> getJwtResponseResponseEntity(HttpServletResponse response, JwtResponse jwtResponse) {
        Cookie refreshCookie = new Cookie("refresh_token", jwtResponse.getRefreshToken());
        refreshCookie.setMaxAge(authorizationService.getTokenLifespan(jwtResponse.getRefreshToken()));
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new JwtResponse(jwtResponse.getAccessToken(), null));
    }

}
