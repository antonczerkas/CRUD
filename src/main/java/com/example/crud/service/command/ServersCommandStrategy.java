package com.example.crud.service.command;

import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import com.example.crud.service.TelegramBotService;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class ServersCommandStrategy implements CommandStrategy {
    @Override
    public String getCommand() {
        return "/servers";
    }

    @Override
    public void execute(Long chatId, TelegramBotService botService) {
        Optional<TelegramUser> userOpt = botService.telegramUserRepository.findById(chatId);
        if (userOpt.isEmpty() || userOpt.get().getRuvdsApiToken() == null) {
            botService.sendNotification(chatId, "❌ API токен не установлен. Используйте /settoken");
            return;
        }
        try {
            RuvdsDTO.ServersListResponse response = botService.ruvdsApiClient.getServers("Bearer " + userOpt.get().getRuvdsApiToken());
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
                    .append("⏰ - если срок оплаты истёк\n/help");

            botService.sendNotification(chatId, message.toString().trim());
        } catch (Exception e) {
            botService.sendNotification(chatId, "⚠️ Ошибка: " + e.getMessage());
        }
    }
}