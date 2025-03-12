package com.example.crud.dto;

import com.example.crud.model.User;
import com.example.crud.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setPassword(user.getPassword());
        userDTO.setEmail(user.getEmail());
        userDTO.setAge(user.getAge());
        if (user.getRoles() != null) {
            userDTO.setRoles(user.getRoles().stream()
                    .map(Role::getRole)
                    .collect(Collectors.toSet()));
        } else {
            userDTO.setRoles(Set.of());
        }
        return userDTO;
    }

    public static User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        if (userDTO.getRoles() != null) {
            user.setRoles(userDTO.getRoles().stream()
                    .map(Role::new)
                    .collect(Collectors.toSet()));
        } else {
            user.setRoles(Set.of());
        }
        return user;
    }
}