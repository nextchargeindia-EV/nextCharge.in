package com.nextcharge.api.service;

import com.nextcharge.api.model.Notification;
import com.nextcharge.api.model.User;
import com.nextcharge.api.repository.NotificationRepository;
import com.nextcharge.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized notification control");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createGeneralNotification(UUID userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(Notification.NotificationType.GENERAL)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }
}
