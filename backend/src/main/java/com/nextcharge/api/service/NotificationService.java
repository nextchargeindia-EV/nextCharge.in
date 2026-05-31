package com.nextcharge.api.service;

import com.nextcharge.api.model.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<Notification> getNotificationsForUser(UUID userId);
    void markAsRead(UUID notificationId, UUID userId);
    void createGeneralNotification(UUID userId, String title, String message);
}
