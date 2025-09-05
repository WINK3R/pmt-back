package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.ProjectMemberResponse;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.ProjectMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMembershipServiceTest {

    @Mock
    private ProjectMembershipRepository repository;

    @InjectMocks
    private ProjectMembershipService service;

    private Project project;
    private User user;
    private ProjectMembership membership;
    private UUID projectId;
    private UUID membershipId;

    @BeforeEach
    void setup() {
        projectId = UUID.randomUUID();
        membershipId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("John");

        membership = new ProjectMembership();
        membership.setId(membershipId);
        membership.setProject(project);
        membership.setUser(user);
        membership.setRole(ProjectMembership.Role.MEMBER);
        membership.setJoinedAt(Instant.now());
    }

    @Test
    void createMembership_shouldSaveAndReturn() {
        when(repository.save(any(ProjectMembership.class))).thenReturn(membership);

        ProjectMembership result = service.createMembership(project, user, ProjectMembership.Role.ADMIN);

        assertNotNull(result);
        assertEquals(ProjectMembership.Role.MEMBER, result.getRole());
        verify(repository).save(any(ProjectMembership.class));
    }

    @Test
    void delete_shouldCallRepository() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(repository).deleteById(id);
    }

    @Test
    void getMemberResponsesByProjectId_shouldMap() {
        when(repository.findByProjectIdWithUser(projectId)).thenReturn(List.of(membership));

        List<ProjectMemberResponse> result = service.getMemberResponsesByProjectId(projectId);

        assertEquals(1, result.size());
        assertEquals(membershipId, result.getFirst().membershipId());
    }

    @Test
    void getMemberResponsesByProjectIdAndRole_shouldMap() {
        when(repository.findByProjectIdAndRoleWithUser(projectId, ProjectMembership.Role.MEMBER))
                .thenReturn(List.of(membership));

        List<ProjectMemberResponse> result = service.getMemberResponsesByProjectIdAndRole(projectId, ProjectMembership.Role.MEMBER);

        assertEquals(1, result.size());
        assertEquals(membershipId, result.getFirst().membershipId());
    }

    @Test
    void createMembershipIfNotExists_shouldReturnExistingWhenAlreadyExists() {
        when(repository.existsByProjectIdAndUserId(projectId, user.getId())).thenReturn(true);

        ProjectMembership result = service.createMembershipIfNotExists(project, user, ProjectMembership.Role.OWNER);

        assertEquals(ProjectMembership.Role.OWNER, result.getRole());
        verify(repository, never()).save(any());
    }

    @Test
    void createMembershipIfNotExists_shouldCreateWhenNotExists() {
        when(repository.existsByProjectIdAndUserId(projectId, user.getId())).thenReturn(false);
        when(repository.save(any(ProjectMembership.class))).thenReturn(membership);

        ProjectMembership result = service.createMembershipIfNotExists(project, user, ProjectMembership.Role.OWNER);

        assertNotNull(result);
        verify(repository).save(any(ProjectMembership.class));
    }

    @Test
    void isMember_shouldDelegateToRepository() {
        when(repository.existsByProjectIdAndUserId(projectId, user.getId())).thenReturn(true);

        assertTrue(service.isMember(projectId, user.getId()));
        verify(repository).existsByProjectIdAndUserId(projectId, user.getId());
    }

    @Test
    void changeRole_shouldUpdateRole() {
        when(repository.findById(membershipId)).thenReturn(Optional.of(membership));
        when(repository.save(any(ProjectMembership.class))).thenReturn(membership);

        ProjectMemberResponse result = service.changeRole(projectId, membershipId, ProjectMembership.Role.ADMIN);

        assertEquals(ProjectMembership.Role.ADMIN, result.role());
        verify(repository).save(membership);
    }

    @Test
    void changeRole_shouldThrowWhenMembershipNotFound() {
        when(repository.findById(membershipId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.changeRole(projectId, membershipId, ProjectMembership.Role.ADMIN));
    }

    @Test
    void changeRole_shouldThrowWhenProjectMismatch() {
        Project otherProject = new Project();
        otherProject.setId(UUID.randomUUID());
        membership.setProject(otherProject);

        when(repository.findById(membershipId)).thenReturn(Optional.of(membership));

        assertThrows(ResponseStatusException.class,
                () -> service.changeRole(projectId, membershipId, ProjectMembership.Role.ADMIN));
    }
}
