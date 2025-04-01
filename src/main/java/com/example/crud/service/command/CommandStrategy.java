package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;

public interface CommandStrategy {
    String getCommand();

    void execute(Long chatId, TelegramBotService botService);
}