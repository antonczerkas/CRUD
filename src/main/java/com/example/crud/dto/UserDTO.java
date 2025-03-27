package com.example.crud.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 20, message = "Логин должен быть от 2 до 20 символов")
    private String name;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 3, max = 10, message = "Пароль должен быть от 3 до 10 символов")
    private String password;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    private String email;

    @NotNull(message = "Возраст не может быть пустым")
    @Min(value = 1, message = "Возраст не может быть меньше 1")
    @Max(value = 120, message = "Возраст не может быть больше 120")
    private int age;

    @Min(value = 1, message = "ID чата Telegram должен быть положительным числом")
    private Long telegramChatId;

    @Size(max = 100, message = "API токен не может быть длиннее 100 символов")
    private String ruvdsApiToken;

    @DecimalMin(value = "0.0", message = "Минимальный баланс не может быть отрицательным")
    private Double minBalanceThreshold;

    @NotNull(message = "Статус уведомлений не может быть пустым")
    private Boolean notificationEnabled;

    @NotEmpty(message = "Роли не могут быть пустыми")
    private Set<String> roles;
}