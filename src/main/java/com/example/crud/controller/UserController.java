package com.example.crud.controller;

import com.example.crud.dto.UserDTO;
import com.example.crud.mapper.UserMapper;
import com.example.crud.model.User;
import com.example.crud.service.UserServiceImp;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserServiceImp userService;

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

    @PostMapping(path = "admin/user")
    public ResponseEntity<?> registerNewUserPage(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }
        User user = UserMapper.toEntity(userDTO);
        userService.saveUser(user);
        log.debug("Выполнено добавление пользователя: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Пользователь успешно создан"));
    }
}