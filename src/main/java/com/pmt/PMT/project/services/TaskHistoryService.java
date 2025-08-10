package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.TaskHistory;
import com.pmt.PMT.project.repositories.TaskHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    public List<TaskHistory> findAll() {
        return taskHistoryRepository.findAll();
    }

    public TaskHistory getById(UUID id) {
        return taskHistoryRepository.findById(id).orElseThrow();
    }

    public TaskHistory create(TaskHistory history) {
        return taskHistoryRepository.save(history);
    }

    public List<TaskHistory> getByTask(Task task) {
        return taskHistoryRepository.findByTask(task);
    }

    public List<TaskHistory> getByTaskOrdered(Task task) {
        return taskHistoryRepository.findByTaskOrderByChangedAtDesc(task);
    }

    public List<TaskHistory> getByTaskIdOrdered(UUID taskId) {
        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);
    }
}
