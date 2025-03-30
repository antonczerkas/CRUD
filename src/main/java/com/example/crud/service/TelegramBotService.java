package com.example.crud.service;

import com.example.crud.client.RuvdsApiClient;
import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import com.example.crud.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {
    private final TelegramUserRepository telegramUserRepository;
    private final RuvdsApiClient ruvdsApiClient;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final Map<Long, String> awaitingInputMap = new ConcurrentHashMap<>();

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
        switch (command) {
            case "/start":
                sendWelcomeMessage(chatId);
                break;
            case "/help":
                sendHelpMessage(chatId);
                break;
            case "/status":
                sendStatus(chatId);
                break;
            case "/enable":
                enableNotifications(chatId, true);
                break;
            case "/disable":
                enableNotifications(chatId, false);
                break;
            case "/settoken":
                prepareForTokenInput(chatId);
                break;
            case "/setthreshold":
                prepareForThresholdInput(chatId);
                break;
            case "/servers":
                sendServersList(chatId);
                break;
            default:
                sendNotification(chatId, "❌ Неизвестная команда. /help - список команд");
        }
    }

    private void sendServersList(Long chatId) {
        TelegramUser user = telegramUserRepository.findById(chatId).orElse(null);
        if (user == null || user.getRuvdsApiToken() == null) {
            sendNotification(chatId, "❌ API токен не установлен. Используйте /settoken");
            return;
        }
        try {
            RuvdsDTO.ServersListResponse response = ruvdsApiClient.getServers("Bearer " + user.getRuvdsApiToken());
            if (response == null || response.getServers() == null || response.getServers().isEmpty()) {
                sendNotification(chatId, "ℹ️ У вас нет серверов в RuVDS");
                return;
            }
            StringBuilder message = new StringBuilder("📋 Список ваших серверов:\n\n");
            for (RuvdsDTO.ServerResponse server : response.getServers()) {
                String ip = (server.getNetworkV4() != null && !server.getNetworkV4().isEmpty())
                        ? server.getNetworkV4().get(0).getIpAddress()
                        : "нет IP";
                String paidTill = "не указана";
                if (server.getPaidTill() != null) {
                    paidTill = ZonedDateTime.parse(server.getPaidTill(), DateTimeFormatter.ISO_DATE_TIME)
                            .withZoneSameInstant(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                }
                String comment = server.getUserComment() != null ? " (" + server.getUserComment() + ")" : "";
                message.append(String.format(
                        "Сервер #%d%s\nIP: %s\nОплата до: %s\n\n",
                        server.getServerId(),
                        comment,
                        ip,
                        paidTill
                ));
            }
            sendNotification(chatId, message.toString().trim());
        } catch (Exception e) {
            sendNotification(chatId, "⚠️ Ошибка: " + e.getMessage());
        }
    }

    private void prepareForTokenInput(Long chatId) {
        awaitingInputMap.put(chatId, "token");
        sendNotification(chatId, "🔑 Введите API токен RuVDS:\n" +
                "[https://ruvds.com/ru-rub/my/settings/api]");
    }

    private void prepareForThresholdInput(Long chatId) {
        awaitingInputMap.put(chatId, "threshold");
        sendNotification(chatId, "💰 Введите минимальный баланс (в российских рублях):");
    }

    private void handleUserInput(Long chatId, String input) {
        String expectedInput = awaitingInputMap.get(chatId);
        awaitingInputMap.remove(chatId);

        if (input.startsWith("/")) {
            handleCommand(chatId, input);
            return;
        }

        switch (expectedInput) {
            case "token":
                saveToken(chatId, input);
                break;
            case "threshold":
                saveThreshold(chatId, input);
                break;
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

    private void sendWelcomeMessage(Long chatId) {
        String message = "Добро пожаловать! Это бот, который следит за балансом и сообщает, когда нужно продлевать сервера RuVDS\n\n" +
                "Создайте токен с разрешением на чтение по адресу:\n" +
                "https://ruvds.com/ru-rub/my/settings/api\n\n" +
                "Используйте /settoken для начала работы\n\n" +
                "Используйте кнопку Меню\n" +
                "либо /help - список команд\n\n" +
                "❗❗❗В рамках теста опрашивает api каждую минуту\n" +
                "Исходный код GitHub:\n" +
                "https://github.com/antonczerkas/CRUD";
        sendNotification(chatId, message);
    }

    private void sendHelpMessage(Long chatId) {
        String message = "📋 Список команд:\n\n" +
                "/settoken - установить API токен RuVDS\n" +
                "/setthreshold - установить минимальный баланс\n" +
                "/status - текущие настройки\n" +
                "/servers - список серверов\n" +
                "/enable - включить уведомления\n" +
                "/disable - выключить уведомления\n" +
                "/help - список команд";
        sendNotification(chatId, message);
    }

    private void sendStatus(Long chatId) {
        Optional<TelegramUser> userOpt = telegramUserRepository.findById(chatId);

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
            sendNotification(chatId, message);
        } else {
            sendNotification(chatId, "ℹ️ Вы еще не зарегистрированы. Используйте /settoken для начала работы." + "\n/help");
        }
    }

    private void enableNotifications(Long chatId, boolean enable) {
        TelegramUser user = telegramUserRepository.findById(chatId)
                .orElseGet(() -> TelegramUser.builder()
                        .telegramChatId(chatId)
                        .notificationEnabled(enable)
                        .minBalanceThreshold(0.0)
                        .build());

        user.setNotificationEnabled(enable);
        telegramUserRepository.save(user);

        String status = enable ? "включены" : "выключены";
        sendNotification(chatId, "✅ Уведомления " + status + "\n/help");
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