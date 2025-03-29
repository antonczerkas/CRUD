package com.example.crud.service;

import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerChecker {
    private final TelegramBotService telegramBot;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final int DAYS_BEFORE_EXPIRE = 999;

    public void checkServers(TelegramUser user, List<RuvdsDTO.ServerResponse> servers) {
        List<String> expiringServers = new ArrayList<>();
        for (RuvdsDTO.ServerResponse server : servers) {
            if (shouldNotifyAboutServer(server)) {
                expiringServers.add(formatServerInfo(server));
            }
        }
        if (!expiringServers.isEmpty()) {
            sendNotification(user, expiringServers);
        }
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