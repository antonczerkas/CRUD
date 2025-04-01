package com.example.crud.service;

import com.example.crud.client.RuvdsApiClient;
import com.example.crud.model.TelegramUser;
import com.example.crud.repository.TelegramUserRepository;
import com.example.crud.service.command.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    public final TelegramUserRepository telegramUserRepository;
    public final RuvdsApiClient ruvdsApiClient;
    private final Map<String, CommandStrategy> commandStrategies;

    public TelegramBotService(
            TelegramUserRepository telegramUserRepository,
            RuvdsApiClient ruvdsApiClient,
            List<CommandStrategy> strategies
    ) {
        this.telegramUserRepository = telegramUserRepository;
        this.ruvdsApiClient = ruvdsApiClient;
        this.commandStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        CommandStrategy::getCommand,
                        Function.identity()
                ));
    }

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    public final Map<Long, String> awaitingInputMap = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            try {
                if (awaitingInputMap.containsKey(chatId)) {
                    handleUserInput(chatId, messageText);
                    return;
                }
                handleCommand(chatId, messageText);
            } catch (Exception e) {
                sendNotification(chatId, "⚠️ Ошибка: " + e.getMessage() + "\n/help");
            }
        }
    }

    private void handleCommand(Long chatId, String command) {
        CommandStrategy strategy = commandStrategies.get(command);
        if (strategy != null) {
            strategy.execute(chatId, this);
        } else {
            sendNotification(chatId, "❌ Неизвестная команда. /help - список команд");
        }
    }

    private void handleUserInput(Long chatId, String input) {
        String expectedInput = awaitingInputMap.get(chatId);
        awaitingInputMap.remove(chatId);

        if (input.startsWith("/")) {
            handleCommand(chatId, input);
            return;
        }

        if ("token".equals(expectedInput)) {
            saveToken(chatId, input);
        } else if ("threshold".equals(expectedInput)) {
            saveThreshold(chatId, input);
        }
    }

    private void saveToken(Long chatId, String token) {
        if (token.trim().isEmpty()) {
            sendNotification(chatId, "❌ Токен не может быть пустым" + "\n/help");
            return;
        }

        TelegramUser user = telegramUserRepository.findById(chatId)
                .orElseGet(() -> TelegramUser.builder()
                        .telegramChatId(chatId)
                        .notificationEnabled(true)
                        .minBalanceThreshold(0.0)
                        .build());

        user.setRuvdsApiToken(token);
        telegramUserRepository.save(user);
        sendNotification(chatId, "✅ API токен успешно сохранён!\n" +
                "/setthreshold установите минимальный баланс");
    }

    private void saveThreshold(Long chatId, String thresholdInput) {
        try {
            double threshold = Double.parseDouble(thresholdInput);

            TelegramUser user = telegramUserRepository.findById(chatId)
                    .orElseGet(() -> TelegramUser.builder()
                            .telegramChatId(chatId)
                            .notificationEnabled(true)
                            .build());

            user.setMinBalanceThreshold(threshold);
            telegramUserRepository.save(user);
            sendNotification(chatId, String.format("✅ Минимальный баланс установлен: %.2f руб." + "\n/help", threshold));
        } catch (NumberFormatException e) {
            sendNotification(chatId, "❌ Неверный формат числа. Пример: 500 или 250.50" + "\n/help");
        }
    }

    public void sendNotification(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}