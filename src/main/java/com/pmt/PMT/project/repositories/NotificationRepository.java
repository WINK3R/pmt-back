package com.pmt.PMT.project.repositories;

import com.pmt.PMT.project.models.Notification;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser(User user);

    List<Notification> findByTask(Task task);

    List<Notification> findByStatus(Notification.Status status);
}
