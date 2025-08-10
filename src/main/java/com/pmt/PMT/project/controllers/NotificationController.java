package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.models.Notification;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.services.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.findAll();
    }

    @GetMapping("/{id}")
    public Notification getById(@PathVariable UUID id) {
        return notificationService.getById(id);
    }

    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        return notificationService.create(notification);
    }

    // Filtres pratiques
    @GetMapping("/by-user/{userId}")
    public List<Notification> getByUser(@PathVariable UUID userId) {
        User u = new User();
        u.setId(userId);
        return notificationService.getByUser(u);
    }

    @GetMapping("/by-task/{taskId}")
    public List<Notification> getByTask(@PathVariable UUID taskId) {
        Task t = new Task();
        t.setId(taskId);
        return notificationService.getByTask(t);
    }

    @GetMapping("/by-status/{status}")
    public List<Notification> getByStatus(@PathVariable Notification.Status status) {
        return notificationService.getByStatus(status);
    }

    // Marquer comme envoyée
    @PatchMapping("/{id}/mark-sent")
    public Notification markSent(@PathVariable UUID id) {
        return notificationService.markSent(id);
    }
}
