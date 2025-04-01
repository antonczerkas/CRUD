package com.example.crud.service.command;

import com.example.crud.model.TelegramUser;
import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

@Component
public class DisableNotificationsCommandStrategy implements CommandStrategy {
    @Override
    public String getCommand() {
        return "/disable";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        setNotificationStatus(chatId, botService, false);
    }

    void setNotificationStatus(Long chatId, TelegramBotService botService, boolean enable) {
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