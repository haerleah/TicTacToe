package s21.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s21.domain.service.GameService;
import s21.domain.service.UserService;
import s21.security.model.JwtAuthentication;
import s21.security.model.JwtRequest;
import s21.security.service.AuthorizationService;
import s21.web.mapper.WebMapper;
import s21.web.model.GameDTO;

import java.util.*;

@Controller
public class ViewController {
    private final GameService gameService;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    @Autowired
    public ViewController(GameService service, UserService userService, AuthorizationService authorizationService) {
        this.gameService = service;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/")
    public String startPage(Model model) {
        model.addAttribute("jwtRequest", new JwtRequest());
        return "start_page";
    }

    @GetMapping("/game_menu")
    public String showGameMenu(Model model) {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        UUID userId = (UUID) jwtAuthentication.getPrincipal();
        model.addAttribute("login", userService.getUser(userId).getLogin());
        return "menu_page";
    }

    @GetMapping("/new")
    public String newGame(@RequestParam("type") String type, @RequestParam("mark") String mark) {
        GameDTO currentGame;
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        if (type.equals("bot")) {
            currentGame = WebMapper.gameToDTO(gameService.createNewGame((UUID) jwtAuthentication.getPrincipal(), mark, true));
            return "redirect:/game/" + currentGame.getUuid();
        } else if (type.equals("online")) {
            currentGame = WebMapper.gameToDTO(gameService.createNewGame((UUID) jwtAuthentication.getPrincipal(), mark, false));
            return "redirect:/game/" + currentGame.getUuid();
        }
        return "redirect:/game_menu";
    }

    @GetMapping("/game/{uuid}")
    public String showGame(@PathVariable("uuid") UUID uuid, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("uuid", uuid.toString());
        try {
            gameService.getGame(uuid);
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("uuid", uuid.toString());
            return "redirect:/uuid_error";
        }
        return "game";
    }

    @GetMapping("/game/join/{uuid}")
    public String joinGame(@PathVariable("uuid") UUID uuid, RedirectAttributes redirectAttributes) {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        try {
            gameService.joinUserToGame(uuid, (UUID) jwtAuthentication.getPrincipal());
            return "redirect:/game/" + uuid;
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("uuid", uuid);
            redirectAttributes.addFlashAttribute("uuid_error", exception.getMessage());
            return "redirect:/uuid_error";
        } catch (TooManyListenersException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/game_menu";
        }
    }

    @GetMapping("/uuid_error")
    public String showUuidError() {
        return "uuid_error";
    }

}
