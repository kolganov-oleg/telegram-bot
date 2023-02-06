package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskCreator;
import pro.sky.telegrambot.service.NotificationTasksSender;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;
    private final NotificationTasksSender sender;
    private final NotificationTaskCreator notificationTaskCreator;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository,
                                      TelegramBot telegramBot,
                                      NotificationTasksSender sender,
                                      NotificationTaskCreator notificationTaskCreator) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
        this.sender = sender;
        this.notificationTaskCreator = notificationTaskCreator;
    }

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }


    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                Long chatId = update.message().chat().id();
                String messageText = update.message().text();
                String welcomeAnswer = "Бонжур! Создавай напоминания в формате - дд.мм.гг чч:мм текст напоминания";
                if (("/start").equals(messageText)) {
                    sender.sendMessage(chatId, welcomeAnswer);

                } else {
                    Matcher matcher = pattern.matcher(messageText);
                    LocalDateTime dateTime;
                    if (matcher.find() && (dateTime = parse(matcher.group(1))) != null) {
                        String message = matcher.group(3);
                        logger.info("Notification task message (date: {}, message: {})", dateTime, message);
                        notificationTaskCreator.create(chatId, message, dateTime);
                        sender.sendMessage(chatId, "Время пошло");
                    } else {
                        sender.sendMessage(chatId, "Неправильный ввод");
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }



}