package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationTasksSender {
    private final Logger logger = LoggerFactory.getLogger(NotificationTasksSender.class);
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationTasksSender(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @Transactional
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void notificationTasksTimer() {
        notificationTaskRepository.findAllByMessageSentTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(notificationTask -> {
                    sendMessage(notificationTask.getChatId(), notificationTask.getMessageText());
                    notificationTaskRepository.delete(notificationTask);
                });
    }
    public void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage(chatId, messageText);
        SendResponse response = telegramBot.execute(message);
        if (!response.isOk()) {
            logger.info(response.toString());
        }
    }
}