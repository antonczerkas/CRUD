package com.example.crud.controller;

import com.example.crud.dto.UserDTO;
import com.example.crud.mapper.UserMapper;
import com.example.crud.model.User;
import com.example.crud.service.UserServiceImp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class AdminController {

    @Autowired
    private UserServiceImp userService;

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
}