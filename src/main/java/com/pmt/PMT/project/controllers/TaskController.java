package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.TaskMinimalRequest;
import com.pmt.PMT.project.dto.TaskResponse;
import com.pmt.PMT.project.services.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable UUID id) {
        return taskService.getExpanded(id);
    }

    @PostMapping
    public TaskResponse create(@RequestBody TaskMinimalRequest req) {
        return taskService.create(req);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id,
                               @RequestBody TaskMinimalRequest req,
                               Authentication authentication) {
        return taskService.update(id, req, authentication);
    }

    @DeleteMapping("/{id}")
    public TaskResponse delete(@PathVariable UUID id, Authentication authentication) {
        return taskService.delete(id, authentication);
    }
}
