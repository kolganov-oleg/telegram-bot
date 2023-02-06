package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;

@Service
public class NotificationTaskCreator {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskCreator(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }
    @Transactional
    public void create(Long chatId, String message, LocalDateTime dateTime) {
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setChatId(chatId);
        notificationTask.setMessageText(message);
        notificationTask.setMessageSentTime(dateTime);
        notificationTaskRepository.save(notificationTask);
    }
}