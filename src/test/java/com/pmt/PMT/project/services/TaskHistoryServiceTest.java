package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.TaskHistoryResponse;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.TaskHistory;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.TaskHistoryRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskHistoryServiceTest {

    @InjectMocks
    private TaskHistoryService service;

    @Mock private TaskHistoryRepository taskHistoryRepository;
    @Mock private UserRepository userRepository;

    private UUID taskId;
    private Task task;
    private User user;

    @BeforeEach
    void setup() {
        taskId = UUID.randomUUID();
        task = new Task();
        task.setId(taskId);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setEmail("john@test.com");
    }

    @Test
    void create_shouldSaveHistory() {
        TaskHistory h = new TaskHistory();
        when(taskHistoryRepository.save(h)).thenReturn(h);

        TaskHistory result = service.create(h);

        assertSame(h, result);
        verify(taskHistoryRepository).save(h);
    }

    @Test
    void findByTaskId_shouldMapToText() {
        TaskHistory h = new TaskHistory();
        h.setField("title");
        h.setOldValue("old");
        h.setNewValue("new");

        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)).thenReturn(List.of(h));

        List<TaskHistoryResponse> result = service.findByTaskId(taskId);

        assertEquals(1, result.size());
        assertEquals("old", result.getFirst().oldValue().getText());
        assertEquals("new", result.getFirst().newValue().getText());
    }

    @Test
    void findByTaskId_shouldMapAssigneeUser() {
        UUID uid = user.getId();

        TaskHistory h = new TaskHistory();
        h.setField("assigneeId");
        h.setNewValue(uid.toString());

        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)).thenReturn(List.of(h));
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        List<TaskHistoryResponse> result = service.findByTaskId(taskId);

        assertTrue(result.getFirst().newValue().isUser());
        assertEquals("john", result.getFirst().newValue().getUser().username());
    }

    @Test
    void findByTaskId_shouldFallbackWhenUserNotFound() {
        UUID uid = UUID.randomUUID();
        TaskHistory h = new TaskHistory();
        h.setField("assigneeId");
        h.setNewValue(uid.toString());

        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)).thenReturn(List.of(h));
        when(userRepository.findById(uid)).thenReturn(Optional.empty());

        List<TaskHistoryResponse> result = service.findByTaskId(taskId);

        assertEquals(uid.toString(), result.getFirst().newValue().getText());
    }

    @Test
    void findByTaskId_shouldFallbackWhenInvalidUUID() {
        TaskHistory h = new TaskHistory();
        h.setField("assigneeId");
        h.setNewValue("not-a-uuid");

        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)).thenReturn(List.of(h));

        List<TaskHistoryResponse> result = service.findByTaskId(taskId);

        assertEquals("not-a-uuid", result.getFirst().newValue().getText());
    }

    @Test
    void saveAll_shouldDoNothingWhenEmpty() {
        service.saveAll(Collections.emptyList());
        verify(taskHistoryRepository, never()).saveAll(any());
    }

    @Test
    void saveAll_shouldSaveWhenNotEmpty() {
        TaskHistory h = new TaskHistory();
        service.saveAll(List.of(h));
        verify(taskHistoryRepository).saveAll(List.of(h));
    }

    @Test
    void entry_shouldCreateHistory() {
        Instant now = Instant.now();
        TaskHistory h = service.entry(task, user, now, "title", "old", "new");

        assertEquals("title", h.getField());
        assertEquals("old", h.getOldValue());
        assertEquals("new", h.getNewValue());
        assertEquals(task, h.getTask());
        assertEquals(user, h.getChangedBy());
    }

    @Test
    void val_shouldCoverAllBranches() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertNull(TaskHistoryService.val(null));
        assertEquals(id.toString(), TaskHistoryService.val(id));
        assertEquals(now.toString(), TaskHistoryService.val(now));
        assertEquals(Task.Status.TODO.name(), TaskHistoryService.val(Task.Status.TODO));

        Task t = new Task();
        t.setId(id);
        assertEquals(id.toString(), TaskHistoryService.val(t));

        User u = new User();
        u.setId(id);
        assertEquals(id.toString(), TaskHistoryService.val(u));

        assertEquals("hello", TaskHistoryService.val("hello"));
    }

    @Test
    void addIfChanged_shouldAddEntryWhenDifferent() {
        List<TaskHistory> acc = new ArrayList<>();
        service.addIfChanged(acc, task, user, Instant.now(), "title", "old", "new");
        assertEquals(1, acc.size());
    }

    @Test
    void addIfChanged_shouldNotAddWhenEqual() {
        List<TaskHistory> acc = new ArrayList<>();
        service.addIfChanged(acc, task, user, Instant.now(), "title", "same", "same");
        assertTrue(acc.isEmpty());
    }
}
