package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;

public class SetThresholdCommandStrategy implements CommandStrategy {
    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        botService.awaitingInputMap.put(chatId, "threshold");
        botService.sendNotification(chatId, "💰 Введите минимальный баланс (в российских рублях):");
    }
}