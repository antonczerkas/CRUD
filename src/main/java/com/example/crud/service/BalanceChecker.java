package com.example.crud.service;

import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import com.example.crud.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceChecker {
    private final TelegramBotService telegramBot;
    private final TelegramUserRepository telegramUserRepository;

    public void checkBalance(TelegramUser user, RuvdsDTO.BalanceResponse balanceResponse) {
        if (shouldNotifyAboutBalance(user, balanceResponse)) {
            boolean shouldSendNotification = user.getLastKnownBalance() == null ||
                    !user.getLastBalanceNotificationSent() ||
                    !user.getLastKnownBalance().equals(balanceResponse.getAmount());
            if (shouldSendNotification) {
                sendNotification(user, balanceResponse.getAmount(), user.getMinBalanceThreshold());
                user.setLastBalanceNotificationSent(true);
            }
        } else {
            user.setLastBalanceNotificationSent(false);
        }
        user.setLastKnownBalance(balanceResponse.getAmount());
        telegramUserRepository.save(user);
    }

    private boolean shouldNotifyAboutBalance(TelegramUser user, RuvdsDTO.BalanceResponse balanceResponse) {
        return balanceResponse != null &&
                balanceResponse.getAmount() != null &&
                user.getMinBalanceThreshold() != null &&
                balanceResponse.getAmount() < user.getMinBalanceThreshold();
    }

    private void sendNotification(TelegramUser user, Double currentBalance, Double threshold) {
        String message = String.format(
                "⚠️ Внимание! Ваш баланс RuVDS составляет %.2fр., что ниже установленного порога в %.2fр.",
                currentBalance, threshold
        );
        telegramBot.sendNotification(user.getTelegramChatId(), message);
    }
}