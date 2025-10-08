package com.example.crud.service;

import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import com.example.crud.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerChecker {

    private final TelegramBotService telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final int DAYS_BEFORE_EXPIRE = 2;

    public void checkServers(TelegramUser user, List<RuvdsDTO.ServerResponse> servers) {
        List<String> expiringServers = servers.stream()
                .filter(this::shouldNotifyAboutServer)
                .map(this::formatServerInfo)
                .collect(Collectors.toList());
        if (!expiringServers.isEmpty()) {
            String serversHash = generateServersHash(expiringServers);
            boolean shouldSendNotification = !serversHash.equals(user.getLastKnownServersHash()) ||
                    !user.getLastServersNotificationSent();
            if (shouldSendNotification) {
                sendNotification(user, expiringServers);
                user.setLastServersNotificationSent(true);
                user.setLastKnownServersHash(serversHash);
            }
        } else {
            user.setLastServersNotificationSent(false);
            user.setLastKnownServersHash(null);
        }
        telegramUserRepository.save(user);
    }

    private String generateServersHash(List<String> serversInfo) {
        return String.valueOf(serversInfo.hashCode());
    }

    private boolean shouldNotifyAboutServer(RuvdsDTO.ServerResponse server) {
        if (server.getPaidTill() == null) return false;
        ZonedDateTime paidTill = parseDate(server.getPaidTill());
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime warningDate = paidTill.minusDays(DAYS_BEFORE_EXPIRE);
        return now.isAfter(warningDate) && now.isBefore(paidTill);
    }

    private String formatServerInfo(RuvdsDTO.ServerResponse server) {
        String ipAddress = getServerIp(server);
        ZonedDateTime paidTill = parseDate(server.getPaidTill());
        return String.format(
                "Сервер #%d%s\nIP: %s\nОплата до: %s",
                server.getServerId(),
                server.getUserComment() != null ? " (" + server.getUserComment() + ")" : "",
                ipAddress,
                paidTill.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );
    }

    private String getServerIp(RuvdsDTO.ServerResponse server) {
        return server.getNetworkV4() != null && !server.getNetworkV4().isEmpty()
                ? server.getNetworkV4().get(0).getIpAddress()
                : "нет IP";
    }

    private ZonedDateTime parseDate(String dateString) {
        return ZonedDateTime.parse(dateString, DATE_FORMATTER);
    }

    private void sendNotification(TelegramUser user, List<String> serversInfo) {
        String message = String.format(
                "⚠️ Внимание! Срок оплаты следующих серверов истекает через %d дня(дней):\n\n%s\n\nПожалуйста, продлите серверы вовремя!",
                DAYS_BEFORE_EXPIRE,
                String.join("\n\n", serversInfo)
        );
        telegramBot.sendNotification(user.getTelegramChatId(), message);
    }
}