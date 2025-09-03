package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.TaskHistoryResponse;
import com.pmt.PMT.project.services.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks/{taskId}/history")
public class TaskHistoryController {

    @Autowired
    private TaskHistoryService taskHistoryService;

    @GetMapping
    public List<TaskHistoryResponse> list(@PathVariable UUID taskId) {
        return taskHistoryService.findByTaskId(taskId);
    }
}
