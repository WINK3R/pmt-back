package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.TaskMinimalRequest;
import com.pmt.PMT.project.dto.TaskResponse;
import com.pmt.PMT.project.models.*;
import com.pmt.PMT.project.repositories.ProjectMembershipRepository;
import com.pmt.PMT.project.repositories.ProjectRepository;
import com.pmt.PMT.project.repositories.TaskRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock private TaskHistoryService taskHistoryService;
    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectMembershipRepository projectMembershipRepository;
    @Mock private Authentication authentication;

    private Task task;
    private UUID taskId;
    private UUID projectId;
    private User user;
    private Project project;
    private User assignee;
    private User createdBy;
    private User updatedBy;

    private final UUID assigneeId = UUID.randomUUID();
    private final UUID createdById = UUID.randomUUID();
    private final UUID updatedById = UUID.randomUUID();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");

        project = new Project();
        project.setId(projectId);

        assignee = new User();
        assignee.setId(assigneeId);

        createdBy = new User();
        createdBy.setId(createdById);

        updatedBy = new User();
        updatedBy.setId(updatedById);


        task = new Task();
        task.setId(taskId);
        task.setTitle("title");
        task.setDescription("desc");
        task.setProject(project);
        task.setAssignee(user);
        task.setCreatedBy(user);
        task.setUpdatedBy(user);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setStatus(Task.Status.TODO);
    }



    private TaskMinimalRequest buildRequest(UUID assigneeId) {
        return new TaskMinimalRequest(
                "title", "desc", "label",
                Instant.now(),
                Task.Priority.HIGH,
                Task.Status.TODO,
                projectId,
                assigneeId,
                createdById,
                updatedById,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void getByProjectId_shouldReturnResponses() {
        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getByProjectId(projectId);

        assertEquals(1, result.size());
        assertEquals(taskId, result.getFirst().id());
    }

    @Test
    void create_shouldCreateTask_withAssignee() {
        TaskMinimalRequest req = buildRequest(assigneeId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(userRepository.findById(createdById)).thenReturn(Optional.of(createdBy));
        when(userRepository.findById(updatedById)).thenReturn(Optional.of(updatedBy));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse res = taskService.create(req);

        assertEquals("title", res.title());
        assertEquals(Task.Status.TODO, res.status());
        assertEquals(assigneeId, res.assignee().id());
    }

    @Test
    void create_shouldCreateTask_withoutAssignee() {
        TaskMinimalRequest req = buildRequest(null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(createdById)).thenReturn(Optional.of(createdBy));
        when(userRepository.findById(updatedById)).thenReturn(Optional.of(updatedBy));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse res = taskService.create(req);

        assertEquals("title", res.title());
        assertNull(res.assignee());
    }

    @Test
    void create_shouldThrow_whenProjectNotFound() {
        TaskMinimalRequest req = buildRequest(assigneeId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.create(req));
    }

    @Test
    void create_shouldThrow_whenAssigneeNotFound() {
        TaskMinimalRequest req = buildRequest(assigneeId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.create(req));
    }

    @Test
    void create_shouldThrow_whenCreatedByNotFound() {
        TaskMinimalRequest req = buildRequest(null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(createdById)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.create(req));
    }

    @Test
    void create_shouldThrow_whenUpdatedByNotFound() {
        TaskMinimalRequest req = buildRequest(null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(createdById)).thenReturn(Optional.of(createdBy));
        when(userRepository.findById(updatedById)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.create(req));
    }

    @Test
    void update_shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> taskService.update(taskId, mock(TaskMinimalRequest.class), authentication));
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        assertThrows(IllegalArgumentException.class, () -> taskService.update(taskId, mock(TaskMinimalRequest.class), authentication));
    }

    @Test
    void update_shouldThrowWhenProjectInvalid() {
        UUID invalidProjectId = UUID.randomUUID();

        TaskMinimalRequest req = new TaskMinimalRequest(
                "title",
                "description",
                "Development",
                Instant.now(),
                Task.Priority.MEDIUM,
                Task.Status.COMPLETED,
                invalidProjectId,
                null,
                createdBy.getId(),
                updatedBy.getId(),
                Instant.now(),
                Instant.now()
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        assertThrows(IllegalArgumentException.class,
                () -> taskService.update(taskId, req, authentication));
    }


    @Test
    void update_shouldUpdateTask() {
        UUID projectId = UUID.randomUUID();

        TaskMinimalRequest req = new TaskMinimalRequest(
                "new-title",
                "new-description",
                "Development",
                Instant.now(),
                Task.Priority.MEDIUM,
                Task.Status.COMPLETED,
                projectId,
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now()
        );

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.update(taskId, req, authentication);

        assertEquals("new-title", result.title());
        assertEquals("new-description", result.description());
        assertEquals(Task.Status.COMPLETED, result.status());
        assertEquals(Task.Priority.MEDIUM, result.priority());
    }

    @Test
    void update_shouldUpdateTaskWithAssignee() {
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        TaskMinimalRequest req = new TaskMinimalRequest(
                "title-with-assignee",
                "desc-with-assignee",
                "LabelX",
                Instant.now(),
                Task.Priority.HIGH,
                Task.Status.IN_PROGRESS,
                projectId,
                assigneeId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now()
        );

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        Project mockProject = new Project();
        mockProject.setId(projectId);

        User assignee = new User();
        assignee.setId(assigneeId);
        assignee.setEmail("assignee@example.com");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.update(taskId, req, authentication);

        assertNotNull(result);
        assertEquals("title-with-assignee", result.title());
        assertEquals(Task.Priority.HIGH, result.priority());
        assertEquals(Task.Status.IN_PROGRESS, result.status());
        assertEquals(assigneeId, result.assignee().id());
    }

    @Test
    void update_shouldHandleAllNullValues() {
        TaskMinimalRequest req = new TaskMinimalRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                projectId,
                null,
                createdById,
                updatedById,
                null,
                null
        );

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.update(taskId, req, authentication);

        assertNotNull(result);
        assertEquals(taskId, result.id());
        assertEquals("title", result.title());
        assertEquals("desc", result.description());
        assertEquals(Task.Status.TODO, result.status());
    }

    @Test
    void update_shouldHandleOldNullAssigned() {
        task.setAssignee(null);
        UUID newAssigneeId = UUID.randomUUID();

        TaskMinimalRequest req = new TaskMinimalRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                projectId,
                newAssigneeId,
                createdById,
                updatedById,
                null,
                null
        );

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        User assignee = new User();
        assignee.setId(newAssigneeId);
        assignee.setEmail("assignee@test.com");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(newAssigneeId)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.update(taskId, req, authentication);

        assertNotNull(result.assignee());
        assertEquals(newAssigneeId, result.assignee().id());
    }

    @Test
    void update_shouldHandleNewNullAssigned() {
        TaskMinimalRequest req = new TaskMinimalRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                projectId,
                null,
                createdById,
                updatedById,
                null,
                null
        );

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.update(taskId, req, authentication);

        assertNull(result.assignee());
    }

    @Test
    void getExpanded_shouldReturnResponse() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getExpanded(taskId);

        assertEquals(taskId, result.id());
    }

    @Test
    void delete_shouldDeleteTask_whenOwner() {
        ProjectMembership membership = new ProjectMembership();
        membership.setUser(user);
        membership.setRole(ProjectMembership.Role.OWNER);
        when(authentication.getName()).thenReturn("user@test.com");
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(projectMembershipRepository.findByProjectIdWithUser(projectId)).thenReturn(List.of(membership));

        TaskResponse result = taskService.delete(taskId, authentication);

        assertEquals(taskId, result.id());
        verify(taskRepository).delete(task);
    }

    @Test
    void delete_shouldDeleteTask_whenAdmin() {
        ProjectMembership membership = new ProjectMembership();
        membership.setUser(user);
        membership.setRole(ProjectMembership.Role.ADMIN);
        when(authentication.getName()).thenReturn("user@test.com");
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(projectMembershipRepository.findByProjectIdWithUser(projectId)).thenReturn(List.of(membership));

        TaskResponse result = taskService.delete(taskId, authentication);

        assertEquals(taskId, result.id());
        verify(taskRepository).delete(task);
    }

    @Test
    void delete_shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> taskService.delete(taskId, authentication));
    }

    @Test
    void delete_shouldThrowWhenNoProject() {
        task.setProject(null);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(IllegalStateException.class, () -> taskService.delete(taskId, authentication));
    }

    @Test
    void delete_shouldThrowWhenUserNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.delete(taskId, authentication));
    }

    @Test
    void delete_shouldThrowWhenNotMember() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(projectMembershipRepository.findByProjectIdWithUser(projectId)).thenReturn(Collections.emptyList());

        assertThrows(SecurityException.class, () -> taskService.delete(taskId, authentication));
    }

    @Test
    void delete_shouldThrowWhenRoleNotAllowed() {
        ProjectMembership membership = new ProjectMembership();
        membership.setUser(user);
        membership.setRole(ProjectMembership.Role.MEMBER);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(projectMembershipRepository.findByProjectIdWithUser(projectId)).thenReturn(List.of(membership));

        assertThrows(SecurityException.class, () -> taskService.delete(taskId, authentication));
    }

    @Test
    void toResponse_shouldReturnUserSummaries_whenAllRelationsPresent() {
        // Arrange
        UUID id = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());

        User createdBy = new User();
        createdBy.setId(UUID.randomUUID());
        User updatedBy = new User();
        updatedBy.setId(UUID.randomUUID());
        User assignee = new User();
        assignee.setId(UUID.randomUUID());

        Task t = new Task();
        t.setId(id);
        t.setTitle("Task 1");
        t.setDescription("desc");
        t.setPriority(Task.Priority.HIGH);
        t.setStatus(Task.Status.TODO);
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        t.setCreatedBy(createdBy);
        t.setUpdatedBy(updatedBy);
        t.setAssignee(assignee);
        t.setProject(project);
        t.setLabel("label");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(t));

        TaskResponse response = taskService.getExpanded(taskId);

        assertNotNull(response.createdBy());
        assertNotNull(response.updatedBy());
        assertNotNull(response.assignee());
        assertNotNull(response.projectId());
    }

    @Test
    void toResponse_shouldReturnNulls_whenRelationsMissing() {
        // Arrange
        UUID id = UUID.randomUUID();
        Task t = new Task();
        t.setId(id);
        t.setTitle("Task 2");
        t.setDescription("desc");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(t));

        TaskResponse response = taskService.getExpanded(taskId);

        assertNull(response.createdBy());
        assertNull(response.updatedBy());
        assertNull(response.assignee());
        assertNull(response.projectId());
    }

}