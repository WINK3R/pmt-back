package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.TaskCreateRequest;
import com.pmt.PMT.project.dto.TaskResponse;
import com.pmt.PMT.project.dto.UserSummary;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.ProjectRepository;
import com.pmt.PMT.project.repositories.TaskRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task getById(UUID id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public List<TaskResponse> getByProjectId(UUID projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public Task create(Task task) {
        return taskRepository.save(task);
    }

    public TaskResponse update(UUID id, TaskCreateRequest req, Authentication auth) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (req.title() != null) task.setTitle(req.title());
        if (req.description() != null) task.setDescription(req.description());
        if (req.dueDate() != null) task.setDueDate(req.dueDate());
        if (req.priority() != null) task.setPriority(req.priority());
        if (req.status() != null) task.setStatus(req.status());
        if (req.label() != null) task.setLabel(req.label());
        if (req.projectId() != null) {
            Project project = projectRepository.findById(req.projectId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
            task.setProject(project);
        }
        if (req.assigneeId() != null) {
            User assignee = userRepository.findById(req.assigneeId()).orElse(null);
            task.setAssignee(assignee);
        }
        String username = auth.getName();
        User updatedBy = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        task.setUpdatedBy(updatedBy);

        task.setUpdatedAt(Instant.now());

        Task updated = taskRepository.save(task);
        return new TaskResponse(updated);
    }



    public TaskResponse create(TaskCreateRequest req) {
        Project project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        User assignee = userRepository.findById(req.assigneeId())
                .orElse(null);

        User createdBy = userRepository.findById(req.createdById())
                .orElseThrow(() -> new IllegalArgumentException("Invalid createdBy ID"));

        User updatedBy = userRepository.findById(req.updatedById())
                .orElseThrow(() -> new IllegalArgumentException("Invalid updatedBy ID"));

        Task task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setCreatedBy(createdBy);
        task.setUpdatedBy(updatedBy);
        task.setDueDate(req.dueDate());
        task.setPriority(req.priority());
        task.setStatus(req.status());
        task.setCreatedAt(req.createdAt());
        task.setUpdatedAt(req.updatedAt());
        task.setLabel(req.label());

        Task savedTask = taskRepository.save(task);
        return new TaskResponse(savedTask);
    }

    public TaskResponse getExpanded(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow();
        return toResponse(task);
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getDueDate(),
                t.getPriority(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                new UserSummary(t.getCreatedBy()),
                new UserSummary(t.getUpdatedBy()),
                new UserSummary(t.getAssignee()),
                t.getProject() != null ? t.getProject().getId() : null,
                t.getLabel()
        );
    }


}
