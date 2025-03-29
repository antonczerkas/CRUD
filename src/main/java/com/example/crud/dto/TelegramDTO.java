package com.example.crud.dto;

import lombok.Data;

@Data
public class TelegramDTO {
    @Data
    public static class SendMessageRequest {
        private Long chatId;
        private String text;
        private String parseMode;
    }

    @Data
    public static class BotConfig {
        private String username;
        private String token;
    }
}