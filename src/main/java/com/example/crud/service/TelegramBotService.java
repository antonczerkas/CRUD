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

    private final Map<String, CommandStrategy> commandStrategies = Map.of(
            "/start", new StartCommandStrategy(),
            "/help", new HelpCommandStrategy(),
            "/status", new StatusCommandStrategy(),
            "/enable", new EnableNotificationsCommandStrategy(true),
            "/disable", new EnableNotificationsCommandStrategy(false),
            "/settoken", new SetTokenCommandStrategy(),
            "/setthreshold", new SetThresholdCommandStrategy(),
            "/servers", new ServersCommandStrategy()
    );

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

    // Интерфейс стратегии
    private interface CommandStrategy {
        void execute(Long chatId, TelegramBotService botService);
    }

    // Конкретные стратегии
    private class StartCommandStrategy implements CommandStrategy {
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

    private class HelpCommandStrategy implements CommandStrategy {
        @Override
        public void execute(Long chatId, TelegramBotService botService) {
            String message = "📋 Список команд:\n\n" +
                    "/settoken - установить API токен RuVDS\n" +
                    "/setthreshold - установить минимальный баланс\n" +
                    "/status - текущие настройки\n" +
                    "/servers - список серверов\n" +
                    "/enable - включить уведомления\n" +
                    "/disable - выключить уведомления\n" +
                    "/help - список команд";
            botService.sendNotification(chatId, message);
        }
    }

    private class StatusCommandStrategy implements CommandStrategy {
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

    private class EnableNotificationsCommandStrategy implements CommandStrategy {
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

    private class SetTokenCommandStrategy implements CommandStrategy {
        @Override
        public void execute(Long chatId, TelegramBotService botService) {
            botService.awaitingInputMap.put(chatId, "token");
            botService.sendNotification(chatId, "🔑 Введите API токен RuVDS:\n" +
                    "[https://ruvds.com/ru-rub/my/settings/api]");
        }
    }

    private class SetThresholdCommandStrategy implements CommandStrategy {
        @Override
        public void execute(Long chatId, TelegramBotService botService) {
            botService.awaitingInputMap.put(chatId, "threshold");
            botService.sendNotification(chatId, "💰 Введите минимальный баланс (в российских рублях):");
        }
    }

    private class ServersCommandStrategy implements CommandStrategy {
        @Override
        public void execute(Long chatId, TelegramBotService botService) {
            TelegramUser user = botService.telegramUserRepository.findById(chatId).orElse(null);
            if (user == null || user.getRuvdsApiToken() == null) {
                botService.sendNotification(chatId, "❌ API токен не установлен. Используйте /settoken");
                return;
            }
            try {
                RuvdsDTO.ServersListResponse response = botService.ruvdsApiClient.getServers("Bearer " + user.getRuvdsApiToken());
                if (response == null || response.getServers() == null || response.getServers().isEmpty()) {
                    botService.sendNotification(chatId, "ℹ️ У вас нет серверов в RuVDS");
                    return;
                }
                StringBuilder message = new StringBuilder("📋 Список ваших серверов:\n\n");
                ZonedDateTime now = ZonedDateTime.now();
                for (RuvdsDTO.ServerResponse server : response.getServers()) {
                    String ip = (server.getNetworkV4() != null && !server.getNetworkV4().isEmpty())
                            ? server.getNetworkV4().get(0).getIpAddress()
                            : "нет IP";
                    String paidTill = "не указана";
                    String paymentEmoji = "";
                    if (server.getPaidTill() != null) {
                        ZonedDateTime paidTillDate = ZonedDateTime.parse(server.getPaidTill(), DateTimeFormatter.ISO_DATE_TIME)
                                .withZoneSameInstant(ZoneId.systemDefault());
                        paidTill = paidTillDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(now, paidTillDate);
                        if (daysBetween < 0) {
                            paymentEmoji = "⏰ ";
                        } else if (daysBetween < 14) {
                            paymentEmoji = "🔴 ";
                        } else if (daysBetween < 30) {
                            paymentEmoji = "🟡 ";
                        } else {
                            paymentEmoji = "🟢 ";
                        }
                    }
                    String comment = server.getUserComment() != null ? " (" + server.getUserComment() + ")" : "";
                    message.append(String.format(
                            "Сервер #%d%s\nIP: %s\n%sОплата до: %s\n\n",
                            server.getServerId(),
                            comment,
                            ip,
                            paymentEmoji,
                            paidTill
                    ));
                }
                message.append("\n🔴 - если осталось меньше 14 дней\n")
                        .append("🟡 - если осталось меньше 30 дней\n")
                        .append("🟢 - если осталось 30 дней и более\n")
                        .append("⏰ - если срок оплаты истёк");

                botService.sendNotification(chatId, message.toString().trim());
            } catch (Exception e) {
                botService.sendNotification(chatId, "⚠️ Ошибка: " + e.getMessage());
            }
        }
    }
}