package com.example.crud.service;

import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceChecker {
    private final TelegramBotService telegramBot;

    public void checkBalance(TelegramUser user, RuvdsDTO.BalanceResponse balanceResponse) {
        if (shouldNotifyAboutBalance(user, balanceResponse)) {
            sendNotification(user, balanceResponse.getAmount(), user.getMinBalanceThreshold());
        }
    }

    private boolean shouldNotifyAboutBalance(TelegramUser user, RuvdsDTO.BalanceResponse balanceResponse) {
        return balanceResponse != null && balanceResponse.getAmount() != null && balanceResponse.getAmount() < user.getMinBalanceThreshold();
    }

    private void sendNotification(TelegramUser user, Double currentBalance, Double threshold) {
        String message = String.format(
                "⚠️ Внимание! Ваш баланс RuVDS составляет %.2fр., что ниже установленного порога в %.2fр.",
                currentBalance, threshold
        );
        telegramBot.sendNotification(user.getTelegramChatId(), message);
    }
}