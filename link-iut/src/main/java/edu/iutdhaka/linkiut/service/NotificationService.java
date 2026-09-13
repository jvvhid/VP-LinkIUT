package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Notification;
import edu.iutdhaka.linkiut.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(AppUser user, AppUser actor, String type, String message, String link) {
        if (user.getId().equals(actor.getId())) {
            return; // Don't notify self
        }
        Notification notification = new Notification(user, actor, type, message, link);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
