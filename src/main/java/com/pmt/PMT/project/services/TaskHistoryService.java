package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.TaskHistoryResponse;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.TaskHistory;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.TaskHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    public TaskHistory create(TaskHistory history) {
        return taskHistoryRepository.save(history);
    }

    public List<TaskHistoryResponse> findByTaskId(UUID taskId) {
        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)
                .stream()
                .map(TaskHistoryResponse::new)
                .toList();
    }

    public void saveAll(List<TaskHistory> entries) {
        if (entries == null || entries.isEmpty()) return;
        taskHistoryRepository.saveAll(entries);
    }

    public TaskHistory entry(Task task, User by, Instant at,
                             String field, String oldVal, String newVal) {
        TaskHistory h = new TaskHistory();
        h.setTask(task);
        h.setChangedBy(by);
        h.setChangedAt(at != null ? at : Instant.now());
        h.setField(field);
        h.setOldValue(oldVal);
        h.setNewValue(newVal);
        return h;
    }

    public static String val(Object v) {
        return switch (v) {
            case null -> null;
            case UUID uuid -> uuid.toString();
            case Instant i -> i.toString();
            case Enum<?> e -> e.name();
            case Task t when t.getId() != null -> t.getId().toString();
            case User u when u.getId() != null -> u.getId().toString();
            default -> String.valueOf(v);
        };
    }

    public void addIfChanged(List<TaskHistory> acc, Task task, User by, Instant at,
                             String field, Object oldVal, Object newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            acc.add(entry(task, by, at, field, val(oldVal), val(newVal)));
        }
    }
}
