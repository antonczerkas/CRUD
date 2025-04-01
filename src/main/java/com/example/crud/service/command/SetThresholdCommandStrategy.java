package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

@Component
public class SetThresholdCommandStrategy implements CommandStrategy {
    @Override
    public String getCommand() {
        return "/setthreshold";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        botService.awaitingInputMap.put(chatId, "threshold");
        botService.sendNotification(chatId, "💰 Введите минимальный баланс (в российских рублях):");
    }
}