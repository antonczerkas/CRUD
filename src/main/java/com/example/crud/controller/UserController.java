package com.example.crud.controller;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserMapper;
import com.example.crud.model.User;
import com.example.crud.service.UserServiceImp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
        return modelAndView;
    }

    @GetMapping("/logout")
    public ModelAndView logoutPage(SecurityContextLogoutHandler logoutHandler, HttpServletResponse response, HttpServletRequest request, Authentication authentication) {
        logoutHandler.logout(request, response, authentication);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }
/*
    @GetMapping(path = "user")
    public User getCurrUser(Authentication authentication) {
        return userService.getUserByName(authentication.getName());
    }*/
    @GetMapping(path = "user")
    public UserDTO getCurrUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }
        User user = userService.getUserByName(authentication.getName());
        if (user == null) {
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
/*
    @GetMapping("admin/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }*/
    @GetMapping("admin/users")
    public List<UserDTO> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new RuntimeException("Список пользователей пуст");
        }
        return users.stream()
                .map(UserMapper::toDTO) // Преобразуем каждый User в UserDTO
                .collect(Collectors.toList());
    }

    @DeleteMapping("admin/user/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "deleted";
    }

    @GetMapping(path = "admin/user/{id}")
    public UserDTO editUserPage(@PathVariable("id") Long id) {
        User user = userService.findUserById(id);
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
                    System.out.println("Validation error: " + error.getField() + " - " + error.getDefaultMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка создания");
        }
        User user = UserMapper.toEntity(userDTO);
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно создан");
    }
}