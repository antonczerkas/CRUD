package com.example.crud.service.command;

import com.example.crud.model.TelegramUser;
import com.example.crud.service.TelegramBotService;

public class EnableNotificationsCommandStrategy implements CommandStrategy {
    private final boolean enable;

    public EnableNotificationsCommandStrategy(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        TelegramUser user = botService.telegramUserRepository.findById(chatId)
                .orElseGet(() -> TelegramUser.builder()
                        .telegramChatId(chatId)
                        .notificationEnabled(enable)
                        .minBalanceThreshold(0.0)
                        .build());

        user.setNotificationEnabled(enable);
        botService.telegramUserRepository.save(user);

        String status = enable ? "включены" : "выключены";
        botService.sendNotification(chatId, "✅ Уведомления " + status + "\n/help");
    }
}