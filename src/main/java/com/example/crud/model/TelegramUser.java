package com.example.crud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "telegram-users")
public class TelegramUser {
    @Id
    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "ruvds_api_token")
    private String ruvdsApiToken;

    @Column(name = "min_balance_threshold")
    private Double minBalanceThreshold;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Column(name = "last_known_balance")
    private Double lastKnownBalance;

    @Column(name = "last_balance_notification_sent")
    private Boolean lastBalanceNotificationSent;

    @Column(name = "last_servers_notification_sent")
    private Boolean lastServersNotificationSent;

    @Column(name = "last_known_servers_hash", length = 32)
    private String lastKnownServersHash;
}