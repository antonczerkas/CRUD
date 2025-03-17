package com.example.crud.controller;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserMapper;
import com.example.crud.model.User;
import com.example.crud.service.UserServiceImp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserServiceImp userService;

    @GetMapping(path = "/")
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView loginPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        log.info("Запрос на страницу входа");
        return modelAndView;
    }

    @GetMapping("/logout")
    public ModelAndView logoutPage(SecurityContextLogoutHandler logoutHandler, HttpServletResponse response, HttpServletRequest request, Authentication authentication) {
        logoutHandler.logout(request, response, authentication);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        log.info("Пользователь {} вышел из системы", authentication.getName());
        return modelAndView;
    }

    @GetMapping(path = "user")
    public UserDTO getCurrUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Попытка доступа к данным пользователя без аутентификации");
            throw new RuntimeException("Пользователь не аутентифицирован");
        }
        String username = authentication.getName();
        User user = userService.getUserByName(username);
        if (user == null) {
            log.error("Пользователь {} не найден", username);
            throw new RuntimeException("Пользователь не найден");
        }
        return UserMapper.toDTO(user);
    }

    @GetMapping(path = "user/page")
    public ModelAndView getUserPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("parts/user_page");
        return modelAndView;
    }

    @GetMapping("admin/users")
    public List<UserDTO> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            log.warn("Список пользователей пуст");
            throw new RuntimeException("Список пользователей пуст");
        }
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("admin/user/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        log.info("Попытка удаления пользователя с id: {}", id);
        userService.deleteUser(id);
        log.info("Выполнено удаление пользователя id: {}", id);
        return "deleted";
    }

    @GetMapping(path = "admin/user/{id}")
    public UserDTO editUserPage(@PathVariable("id") Long id) {
        log.debug("Запрос данных пользователя для редактирования, id: {}", id);
        User user = userService.findUserById(id);
        if (user == null) {
            log.error("Пользователь с id {} не найден", id);
            throw new RuntimeException("Пользователь не найден");
        }
        return UserMapper.toDTO(user);
    }

    @GetMapping(path = "admin/page")
    public ModelAndView getAdminPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("parts/admin_page");
        return modelAndView;
    }

    @PostMapping(path = "admin/user")
    public ResponseEntity<String> registerNewUserPage(@Valid UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    log.warn("Ошибка валидации: {} - {}", error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка создания");
        }
        User user = UserMapper.toEntity(userDTO);
        userService.saveUser(user);
        log.debug("Выполнено добавление пользователя: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно создан");
    }
}