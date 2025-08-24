package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.TaskCreateRequest;
import com.pmt.PMT.project.dto.TaskResponse;
import com.pmt.PMT.project.models.TaskHistory;
import com.pmt.PMT.project.services.TaskHistoryService;
import com.pmt.PMT.project.services.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable UUID id) {
        return taskService.getExpanded(id);
    }

    @PostMapping
    public TaskResponse create(@RequestBody TaskCreateRequest req) {
        return taskService.create(req);
    }

    @GetMapping("/{id}/history")
    public List<TaskHistory> getHistoryForTask(@PathVariable UUID id) {
        return taskHistoryService.getByTaskIdOrdered(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id,
                               @RequestBody TaskCreateRequest req,
                               Authentication authentication) {
        return taskService.update(id, req, authentication);
    }
}
