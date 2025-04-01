package com.example.crud.service.command;

import com.example.crud.model.TelegramUser;
import com.example.crud.service.TelegramBotService;

import java.util.Optional;

public class StatusCommandStrategy implements CommandStrategy {
    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        Optional<TelegramUser> userOpt = botService.telegramUserRepository.findById(chatId);
        if (userOpt.isPresent()) {
            TelegramUser user = userOpt.get();
            String message = String.format(
                    "⚙️ Текущие настройки:\n\n" +
                            "API токен: %s\n" +
                            "Минимальный баланс: %.2f руб.\n" +
                            "Уведомления: %s" + "\n/help",
                    user.getRuvdsApiToken() != null ? "установлен" : "не установлен",
                    user.getMinBalanceThreshold(),
                    user.getNotificationEnabled() ? "включены" : "выключены"
            );
            botService.sendNotification(chatId, message);
        } else {
            botService.sendNotification(chatId, "ℹ️ Вы еще не зарегистрированы. Используйте /settoken для начала работы." + "\n/help");
        }
    }
}