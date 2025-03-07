package com.example.crud.service;

import com.example.crud.model.User;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    @Transactional
    void saveUser(User user);

    @Transactional
    void deleteUser(Long id);
}