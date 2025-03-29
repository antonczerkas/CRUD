package com.example.crud.service;

import com.example.crud.client.RuvdsApiClient;
import com.example.crud.dto.RuvdsDTO;
import com.example.crud.model.TelegramUser;
import com.example.crud.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuvdsService {
    private final TelegramUserRepository telegramUserRepository;
    private final RuvdsApiClient ruvdsApiClient;
    private final BalanceChecker balanceChecker;
    private final ServerChecker serverChecker;

    @Scheduled(fixedRate = 60000)
    public void checkNotifications() {
        List<TelegramUser> users = telegramUserRepository.findAllByNotificationEnabled(true);
        for (TelegramUser user : users) {
            if (user.getRuvdsApiToken() != null && user.getTelegramChatId() != null) {
                checkUser(user);
            }
        }
    }

    private void checkUser(TelegramUser user) {
        try {
            checkUserBalance(user);
            checkUserServers(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkUserBalance(TelegramUser user) {
        try {
            RuvdsDTO.BalanceResponse response = ruvdsApiClient.getBalance("Bearer " + user.getRuvdsApiToken());
            balanceChecker.checkBalance(user, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkUserServers(TelegramUser user) {
        try {
            RuvdsDTO.ServersListResponse response = ruvdsApiClient.getServers("Bearer " + user.getRuvdsApiToken());
            if (response != null && response.getServers() != null) {
                serverChecker.checkServers(user, response.getServers());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}