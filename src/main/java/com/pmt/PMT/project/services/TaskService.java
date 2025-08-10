package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task getById(UUID id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public Task create(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getByProject(Project project) {
        return taskRepository.findByProject(project);
    }

    public List<Task> getByAssignee(User assignee) {
        return taskRepository.findByAssignee(assignee);
    }

    public List<Task> getByProjectAndStatus(Project project, Task.Status status) {
        return taskRepository.findByProjectAndStatus(project, status);
    }
}
