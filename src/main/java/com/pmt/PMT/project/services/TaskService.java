package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.TaskMinimalRequest;
import com.pmt.PMT.project.dto.TaskResponse;
import com.pmt.PMT.project.dto.UserSummary;
import com.pmt.PMT.project.models.*;
import com.pmt.PMT.project.repositories.ProjectMembershipRepository;
import com.pmt.PMT.project.repositories.ProjectRepository;
import com.pmt.PMT.project.repositories.TaskRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
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
    @Autowired
    private TaskHistoryService taskHistoryService;
    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;

    public List<TaskResponse> getByProjectId(UUID projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse update(UUID id, TaskMinimalRequest req, Authentication auth) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        String username = auth.getName();
        User updatedBy = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldTitle       = task.getTitle();
        String oldDescription = task.getDescription();
        var    oldDueDate     = task.getDueDate();
        var    oldPriority    = task.getPriority();
        var    oldStatus      = task.getStatus();
        var    oldLabel       = task.getLabel();
        UUID   oldAssigneeId  = task.getAssignee() != null ? task.getAssignee().getId() : null;

        if (req.title() != null)       task.setTitle(req.title());
        if (req.description() != null)  task.setDescription(req.description());
        task.setDueDate(req.dueDate());
        if (req.priority() != null)    task.setPriority(req.priority());
        if (req.status() != null)      task.setStatus(req.status());
        if (req.label() != null)       task.setLabel(req.label());
        if (req.assigneeId() == null) {
            task.setAssignee(null);
        } else {
            User assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid assignee ID"));
            task.setAssignee(assignee);
        }

        task.setUpdatedBy(updatedBy);
        Instant now = Instant.now();
        task.setUpdatedAt(now);

        Task updated = taskRepository.save(task);

        var history = new ArrayList<TaskHistory>();
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "title",       oldTitle,       updated.getTitle());
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "description", oldDescription, updated.getDescription());
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "dueDate",     oldDueDate,     updated.getDueDate());
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "priority",    oldPriority,    updated.getPriority());
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "status",      oldStatus,      updated.getStatus());
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "label",       oldLabel,       updated.getLabel());

        UUID newAssigneeId = updated.getAssignee() != null ? updated.getAssignee().getId() : null;
        taskHistoryService.addIfChanged(history, updated, updatedBy, now, "assigneeId", oldAssigneeId, newAssigneeId);

        taskHistoryService.saveAll(history);

        return new TaskResponse(updated);
    }

    public TaskResponse create(TaskMinimalRequest req) {
        Project project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        User assignee = null;
        if (req.assigneeId() != null) {
            assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid assignee ID"));
        }


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
                t.getCreatedBy() != null ? new UserSummary(t.getCreatedBy()) : null,
                t.getUpdatedBy() != null ? new UserSummary(t.getUpdatedBy()) : null,
                t.getAssignee() != null ? new UserSummary(t.getAssignee()) : null,
                t.getProject() != null ? t.getProject().getId() : null,
                t.getLabel()
        );
    }

    @Transactional
    public TaskResponse delete(UUID id, Authentication auth) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getProject() == null) {
            throw new IllegalStateException("Task is not associated with any project");
        }

        String username = auth.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UUID projectId = task.getProject().getId();

        ProjectMembership membership = projectMembershipRepository.findByProjectIdWithUser(projectId).stream()
                .filter(pm -> pm.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("You are not a member of this project"));

        ProjectMembership.Role role = membership.getRole();

        if (role != ProjectMembership.Role.OWNER && role != ProjectMembership.Role.ADMIN) {
            throw new SecurityException("Only OWNER or ADMINISTRATOR can delete tasks");
        }

        taskRepository.delete(task);
        return new TaskResponse(task);
    }

}
