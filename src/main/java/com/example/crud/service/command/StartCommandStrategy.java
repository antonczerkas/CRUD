package com.example.crud.service.command;

import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

@Component
public class StartCommandStrategy implements CommandStrategy {
    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        String message = "Добро пожаловать! Это бот, который следит за балансом и сообщает, когда нужно продлевать сервера RuVDS\n\n" +
                "Создайте токен с разрешением на чтение по адресу:\n" +
                "https://ruvds.com/ru-rub/my/settings/api\n\n" +
                "Используйте /settoken для начала работы\n\n" +
                "Используйте кнопку Меню\n" +
                "либо /help - список команд\n\n" +
                "❗❗❗В рамках теста опрашивает api каждую минуту\n" +
                "Исходный код GitHub:\n" +
                "https://github.com/antonczerkas/CRUD";
        botService.sendNotification(chatId, message);
    }
}