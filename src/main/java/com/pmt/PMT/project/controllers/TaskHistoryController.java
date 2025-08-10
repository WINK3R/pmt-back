package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.TaskHistory;
import com.pmt.PMT.project.services.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task-history")
public class TaskHistoryController {

    @Autowired
    private TaskHistoryService taskHistoryService;

    @GetMapping
    public List<TaskHistory> getAll() {
        return taskHistoryService.findAll();
    }

    @GetMapping("/{id}")
    public TaskHistory getById(@PathVariable UUID id) {
        return taskHistoryService.getById(id);
    }

    @PostMapping
    public TaskHistory create(@RequestBody TaskHistory history) {
        return taskHistoryService.create(history);
    }

    // Récupérer l’historique par tâche
    @GetMapping("/by-task/{taskId}")
    public List<TaskHistory> getByTask(@PathVariable UUID taskId) {
        Task task = new Task();
        task.setId(taskId);
        return taskHistoryService.getByTask(task);
    }

    // Récupérer l’historique par tâche trié par date de modification
    @GetMapping("/by-task-ordered/{taskId}")
    public List<TaskHistory> getByTaskOrdered(@PathVariable UUID taskId) {
        Task task = new Task();
        task.setId(taskId);
        return taskHistoryService.getByTaskOrdered(task);
    }
}
