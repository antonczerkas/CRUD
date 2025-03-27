package com.example.crud.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDTO {
    @Min(value = 1, message = "ID чата Telegram должен быть положительным числом")
    private Long telegramChatId;

    @Size(max = 100, message = "API токен не может быть длиннее 100 символов")
    private String ruvdsApiToken;

    @DecimalMin(value = "0.0", message = "Минимальный баланс не может быть отрицательным")
    private Double minBalanceThreshold;

    @NotNull(message = "Статус уведомлений не может быть пустым")
    private Boolean notificationEnabled;
}