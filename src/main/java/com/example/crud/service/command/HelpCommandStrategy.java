package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;

public class HelpCommandStrategy implements CommandStrategy {
    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        String message = "📋 Список команд:\n\n" +
                "/settoken - установить API токен RuVDS\n" +
                "/setthreshold - установить минимальный баланс\n" +
                "/status - текущие настройки\n" +
                "/servers - список серверов\n" +
                "/enable - включить уведомления\n" +
                "/disable - выключить уведомления\n" +
                "/help - список команд";
        botService.sendNotification(chatId, message);
    }
}