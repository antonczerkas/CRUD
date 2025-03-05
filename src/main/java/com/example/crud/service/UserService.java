package com.example.crud.service;

import com.example.crud.model.User;
import com.example.crud.model.Role;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    @Transactional
    void saveUser(User user);

    @Transactional
    void deleteUser(Long id);

    List<Role> getAllRoles();

    @Transactional
    void updateUserRoles(Long userId, Set<Role> roles);
}