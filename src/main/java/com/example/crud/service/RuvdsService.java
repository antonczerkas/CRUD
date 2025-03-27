package com.example.crud.service;

import com.example.crud.model.User;
import com.example.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuvdsService {
    private final UserRepository userRepository;
    private final TelegramBotService telegramBot;
    private final RestTemplate restTemplate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final int DAYS_BEFORE_EXPIRE = 2;

    @Scheduled(fixedRate = 43200000) // Проверка каждыe 12 часов, 20000 для каждые 20 секунд
    public void checkBalances() {
        List<User> users = userRepository.findAllByNotificationEnabled(true);
        for (User user : users) {
            if (user.getRuvdsApiToken() != null && user.getTelegramChatId() != null) {
                checkUserBalance(user);
                checkServersExpiration(user);
            }
        }
    }

    private void checkUserBalance(User user) {
        try {
            HttpHeaders headers = createHeaders(user);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.ruvds.com/v2/balance",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Double balance = (Double) response.getBody().get("amount");
                if (balance != null && user.getMinBalanceThreshold() != null
                        && balance < user.getMinBalanceThreshold()) {
                    String message = String.format(
                            "⚠️ Внимание! Ваш баланс RuVDS составляет %.2fр., что ниже установленного порога в %.2fр.",
                            balance, user.getMinBalanceThreshold()
                    );
                    telegramBot.sendNotification(user.getTelegramChatId(), message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkServersExpiration(User user) {
        try {
            HttpHeaders headers = createHeaders(user);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.ruvds.com/v2/servers?get_paid_till=true",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> servers = (List<Map<String, Object>>) response.getBody().get("servers");
                List<String> expiringServers = new ArrayList<>();
                for (Map<String, Object> server : servers) {
                    String paidTillStr = (String) server.get("paid_till");
                    if (paidTillStr != null) {
                        ZonedDateTime paidTill = ZonedDateTime.parse(paidTillStr, DATE_FORMATTER);
                        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
                        ZonedDateTime warningDate = paidTill.minusDays(DAYS_BEFORE_EXPIRE);

                        if (now.isAfter(warningDate) && now.isBefore(paidTill)) {
                            Integer serverId = (Integer) server.get("virtual_server_id");
                            expiringServers.add(String.format(
                                    "Сервер #%d (оплата до %s)",
                                    serverId,
                                    paidTill.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                            ));
                        }
                    }
                }
                if (!expiringServers.isEmpty()) {
                    String message = "⚠️ Внимание! Срок оплаты следующих серверов истекает через " +
                            DAYS_BEFORE_EXPIRE + " дня(дней):\n\n" +
                            String.join("\n", expiringServers) +
                            "\n\nПожалуйста, продлите серверы вовремя!";
                    telegramBot.sendNotification(user.getTelegramChatId(), message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpHeaders createHeaders(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + user.getRuvdsApiToken());
        return headers;
    }
}