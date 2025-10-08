package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandStrategy implements CommandStrategy {

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        String message = "📋 Список команд:\n\n" +
                "/settoken - установить API токен RuVDS\n" +
                "/setthreshold - установить минимальный баланс\n" +
                "/status - текущие настройки\n" +
                "/servers - список серверов\n" +
                "/balance - баланс на аккаунте\n" +
                "/enable - включить уведомления\n" +
                "/disable - выключить уведомления\n" +
                "/help - список команд";
        botService.sendNotification(chatId, message);
    }
}