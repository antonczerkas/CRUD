package com.example.crud.service.command;

import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BalanceCommandStrategy implements CommandStrategy {
    @Override
    public String getCommand() {
        return "/balance";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        Optional<TelegramUser> userOpt = botService.telegramUserRepository.findById(chatId);
        if (userOpt.isEmpty() || userOpt.get().getRuvdsApiToken() == null) {
            botService.sendNotification(chatId, "❌ API токен не установлен. Используйте /settoken");
            return;
        }
        try {
            RuvdsDTO.BalanceResponse response = botService.ruvdsApiClient.getBalance("Bearer " + userOpt.get().getRuvdsApiToken());
            String message = new String("💰 Ваш баланс состовляет: " + response.getAmount() + " руб.\n/help");
            botService.sendNotification(chatId, message);
        } catch (Exception e) {
            botService.sendNotification(chatId, "⚠️ Ошибка: " + e.getMessage());
        }
    }
}