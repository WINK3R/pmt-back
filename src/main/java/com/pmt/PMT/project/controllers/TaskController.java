package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.TaskHistory;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.services.TaskHistoryService;
import com.pmt.PMT.project.services.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskHistoryService taskHistoryService;

    @GetMapping
    public List<Task> getAll() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable UUID id) {
        return taskService.getById(id);
    }

    @PostMapping
    public Task create(@RequestBody Task task) {
        return taskService.create(task);
    }

    // Récupérer les tâches par projet
    @GetMapping("/by-project/{projectId}")
    public List<Task> getByProject(@PathVariable UUID projectId) {
        Project project = new Project();
        project.setId(projectId);
        return taskService.getByProject(project);
    }

    // Récupérer les tâches par assigné
    @GetMapping("/by-assignee/{assigneeId}")
    public List<Task> getByAssignee(@PathVariable UUID assigneeId) {
        User user = new User();
        user.setId(assigneeId);
        return taskService.getByAssignee(user);
    }

    // Récupérer les tâches par projet et statut
    @GetMapping("/by-project-status/{projectId}/{status}")
    public List<Task> getByProjectAndStatus(@PathVariable UUID projectId, @PathVariable Task.Status status) {
        Project project = new Project();
        project.setId(projectId);
        return taskService.getByProjectAndStatus(project, status);
    }

    @GetMapping("/{id}/history")
    public List<TaskHistory> getHistoryForTask(@PathVariable UUID id) {
        return taskHistoryService.getByTaskIdOrdered(id);
    }
}
