package com.example.crud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

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
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("/start".equals(messageText)) {
                handleStartCommand(chatId);
            } else {
                handleUnknownCommand(chatId);
            }
        }
    }

    private void handleStartCommand(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Добро пожаловать! Пожалуйста, посетите <a href=\"http://194.87.94.5:8081\">сайт</a>, чтобы заполнить данные для получения уведомлений.\n\n"
                + "Ваш ID чата Telegram: <pre>" + chatId + "</pre>\n"
                + "Ваш токен RuVDS можно получить по <a href=\"https://ruvds.com/ru-rub/my/settings/api\">адресу</a>\n\n\n"
                + "<code>❗❗❗бот находится в разработке, функционал и взаимодействие будут меняться. в рамках теста для сайта:\n"
                + "USER - логин: qwe, пароль: qwe\nADMIN - логин: admin, пароль: admin</code>");
        message.setParseMode("HTML");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleUnknownCommand(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Я не понимаю эту команду. Используйте /start для начала работы.");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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