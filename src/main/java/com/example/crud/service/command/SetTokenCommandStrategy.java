package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

@Component
public class SetTokenCommandStrategy implements CommandStrategy {

    @Override
    public String getCommand() {
        return "/settoken";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        botService.awaitingInputMap.put(chatId, "token");
        botService.sendNotification(chatId, "🔑 Введите API токен RuVDS:\n" +
                "[https://ruvds.com/ru-rub/my/settings/api]");
    }
}