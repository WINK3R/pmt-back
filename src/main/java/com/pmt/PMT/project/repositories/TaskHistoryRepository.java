package com.pmt.PMT.project.repositories;

import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {

    List<TaskHistory> findByTask(Task task);
    List<TaskHistory> findByTaskOrderByChangedAtDesc(Task task);
    List<TaskHistory> findByTaskIdOrderByChangedAtDesc(UUID taskId);
}
