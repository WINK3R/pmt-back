package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.Notification;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification getById(UUID id) {
        return notificationRepository.findById(id).orElseThrow();
    }

    public Notification create(Notification notif) {
        if (notif.getCreatedAt() == null) {
            notif.setCreatedAt(Instant.now());
        }
        return notificationRepository.save(notif);
    }

    public List<Notification> getByUser(User user) {
        return notificationRepository.findByUser(user);
    }

    public List<Notification> getByTask(Task task) {
        return notificationRepository.findByTask(task);
    }

    public List<Notification> getByStatus(Notification.Status status) {
        return notificationRepository.findByStatus(status);
    }

    public Notification markSent(UUID id) {
        Notification n = getById(id);
        n.setStatus(Notification.Status.SENT);
        n.setSentAt(Instant.now());
        return notificationRepository.save(n);
    }
}
