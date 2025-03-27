package com.example.crud.mapper;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserSettingsDTO;
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
        userDTO.setTelegramChatId(user.getTelegramChatId());
        userDTO.setRuvdsApiToken(user.getRuvdsApiToken());
        userDTO.setMinBalanceThreshold(user.getMinBalanceThreshold());
        userDTO.setNotificationEnabled(user.getNotificationEnabled());
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
        user.setTelegramChatId(userDTO.getTelegramChatId());
        user.setRuvdsApiToken(userDTO.getRuvdsApiToken());
        user.setMinBalanceThreshold(userDTO.getMinBalanceThreshold());
        user.setNotificationEnabled(userDTO.getNotificationEnabled());
        if (userDTO.getRoles() != null) {
            user.setRoles(userDTO.getRoles().stream()
                    .map(Role::new)
                    .collect(Collectors.toSet()));
        } else {
            user.setRoles(Set.of());
        }
        return user;
    }

    public static UserSettingsDTO toSettingsDTO(User user) {
        UserSettingsDTO settingsDTO = new UserSettingsDTO();
        settingsDTO.setTelegramChatId(user.getTelegramChatId());
        settingsDTO.setRuvdsApiToken(user.getRuvdsApiToken());
        settingsDTO.setMinBalanceThreshold(user.getMinBalanceThreshold());
        settingsDTO.setNotificationEnabled(user.getNotificationEnabled());
        return settingsDTO;
    }

    public static void toSettingsEntity(User user, UserSettingsDTO settingsDTO) {
        user.setTelegramChatId(settingsDTO.getTelegramChatId());
        user.setRuvdsApiToken(settingsDTO.getRuvdsApiToken());
        user.setMinBalanceThreshold(settingsDTO.getMinBalanceThreshold());
        user.setNotificationEnabled(settingsDTO.getNotificationEnabled());
    }
}