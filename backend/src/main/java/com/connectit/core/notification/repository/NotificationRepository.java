package com.connectit.core.notification.repository;

import com.connectit.core.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderBySentAtDesc(Long recipientId);
    List<Notification> findByRecipientIdAndIsReadOrderBySentAtDesc(Long recipientId, boolean isRead);
}
