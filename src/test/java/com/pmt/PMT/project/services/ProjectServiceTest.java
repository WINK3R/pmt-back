package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.*;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.ProjectRepository;
import com.pmt.PMT.project.repositories.TaskRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectMembershipService projectMembershipService;
    @Mock private TaskRepository taskRepository;
    @Mock private Authentication authentication;

    private User user;
    private Project project;
    private UUID projectId;

    @BeforeEach
    void setup() {
        projectId = UUID.randomUUID();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setProfileImageUrl("avatar.png");

        project = new Project();
        project.setId(projectId);
        project.setName("My Project");
        project.setDescription("Description");
        project.setTag("DEV");
        project.setStartDate(LocalDate.now());
        project.setCreatedAt(Instant.now());
        project.setCreatedBy(user);
    }

    @Test
    void list_shouldReturnAllProjects() {
        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(taskRepository.countByProjectId(projectId)).thenReturn(5L);
        when(taskRepository.countByProjectIdAndStatus(projectId, Task.Status.COMPLETED)).thenReturn(2L);

        List<ProjectListItem> result = projectService.list();

        assertEquals(1, result.size());
        assertEquals("My Project", result.getFirst().name());
        assertEquals(3, result.getFirst().openTasks());
        assertEquals(2, result.getFirst().completedTasks());
    }

    @Test
    void getByIdForMember_shouldReturnDetail_whenUserIsMember() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findWithCreatedBy(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipService.getMemberResponsesByProjectId(projectId))
                .thenReturn(List.of(
                        new ProjectMemberResponse(
                                UUID.randomUUID(),
                                ProjectMembership.Role.MEMBER,
                                Instant.now(),
                                new UserSummary(user)
                        )
                ));

        ProjectResponse res = projectService.getByIdForMember(projectId, authentication);

        assertEquals(projectId, res.id());
        assertEquals("My Project", res.name());
    }


    @Test
    void getByIdForMember_shouldThrow_whenUserNotFound() {
        assertThrows(UsernameNotFoundException.class,
                () -> projectService.getByIdForMember(projectId, authentication));
    }

    @Test
    void getByIdForMember_shouldThrow_whenProjectNotFound() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findWithCreatedBy(projectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> projectService.getByIdForMember(projectId, authentication));
    }

    @Test
    void getByIdForMember_shouldThrow_whenUserNotMember() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findWithCreatedBy(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipService.getMemberResponsesByProjectId(projectId))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class,
                () -> projectService.getByIdForMember(projectId, authentication));
    }

    @Test
    void listByUser_shouldReturnProjects() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByMemberOrderByCreatedAtDesc(user)).thenReturn(List.of(project));
        when(taskRepository.countByProjectId(projectId)).thenReturn(10L);
        when(taskRepository.countByProjectIdAndStatus(projectId, Task.Status.COMPLETED)).thenReturn(4L);

        List<ProjectListItem> result = projectService.listByUser(authentication);

        assertEquals(1, result.size());
        assertEquals("My Project", result.getFirst().name());
        assertEquals(6, result.getFirst().openTasks());
    }

    @Test
    void listByUser_shouldThrow_whenUserNotFound() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> projectService.listByUser(authentication));
    }

    @Test
    void create_shouldCreateProject() {
        ProjectCreateRequest req = new ProjectCreateRequest(
                "New Project",
                "Desc",
                LocalDate.now(),
                "TAG"
                );

        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjectListItem result = projectService.create(req, authentication);

        assertEquals("New Project", result.name());
        verify(projectMembershipService).createMembership(any(Project.class), eq(user), eq(ProjectMembership.Role.OWNER));
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        ProjectCreateRequest req = new ProjectCreateRequest("X", "Y", LocalDate.now(), "Z");
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> projectService.create(req, authentication));
    }

    @Test
    void update_shouldUpdateProject_whenUserIsAdminOrOwner() {
        ProjectMinimalRequest req = new ProjectMinimalRequest(UUID.randomUUID(), "Updated Name", "Updated Description", "Development");

        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipService.getMemberResponsesByProjectId(projectId))
                .thenReturn(List.of(new ProjectMemberResponse(
                        UUID.randomUUID(),
                        ProjectMembership.Role.ADMIN,
                        Instant.now(),
                        new UserSummary(user)
                )));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse res = projectService.update(projectId, req, authentication);

        assertEquals("Updated Name", res.name());
        assertEquals("Updated Description", res.description());
        assertEquals("Development", res.tag());
    }

    @Test
    void update_shouldThrow_whenUserNotFound() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());

        ProjectMinimalRequest req = new ProjectMinimalRequest(UUID.randomUUID(), "Updated Name", "Updated Description", "Development");

        assertThrows(UsernameNotFoundException.class,
                () -> projectService.update(projectId, req, authentication));
    }

    @Test
    void update_shouldThrow_whenProjectNotFound() {
        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        ProjectMinimalRequest req = new ProjectMinimalRequest(UUID.randomUUID(), "Updated Name", "Updated Description", "Development");

        assertThrows(EntityNotFoundException.class,
                () -> projectService.update(projectId, req, authentication));
    }

    @Test
    void update_shouldThrow_whenUserIsNotOwnerOrAdmin() {
        ProjectMinimalRequest req = new ProjectMinimalRequest(UUID.randomUUID(), "Updated Name", "Updated Description", "Development");

        when(authentication.getName()).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipService.getMemberResponsesByProjectId(projectId))
                .thenReturn(List.of(new ProjectMemberResponse(
                        UUID.randomUUID(),
                        ProjectMembership.Role.MEMBER,
                        Instant.now(),
                        new UserSummary(user)
                )));

        assertThrows(SecurityException.class,
                () -> projectService.update(projectId, req, authentication));
    }

}
