package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.ProjectMemberResponse;
import com.pmt.PMT.project.models.*;
import com.pmt.PMT.project.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.pmt.PMT.project.dto.InvitationCreateRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @InjectMocks
    private InvitationService invitationService;

    @Mock private InvitationRepository invitationRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectMembershipService projectMembershipService;
    @Mock private Authentication authentication;

    private User inviter;
    private User invited;
    private Project project;
    private Invitation invitation;
    private UUID projectId;
    private UUID invitationId;

    @BeforeEach
    void setup() {
        projectId = UUID.randomUUID();
        invitationId = UUID.randomUUID();

        inviter = new User();
        inviter.setId(UUID.randomUUID());
        inviter.setEmail("inviter@test.com");

        invited = new User();
        invited.setId(UUID.randomUUID());
        invited.setEmail("invited@test.com");

        project = new Project();
        project.setId(projectId);

        invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setProject(project);
        invitation.setInviter(inviter);
        invitation.setInvited(invited);
        invitation.setStatus(Invitation.Status.PENDING);
    }

    // -------- listByUser --------

    @Test
    void listByUser_shouldThrow_whenUserNotFound() {
        when(authentication.getName()).thenReturn("missing@test.com");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> invitationService.listByUser(authentication));
    }

    @Test
    void listByUser_shouldReturnInvitations() {
        when(authentication.getName()).thenReturn("invited@test.com");
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(invitationRepository.findByInvitedOrderByCreatedAtDesc(invited))
                .thenReturn(List.of(invitation));

        var result = invitationService.listByUser(authentication);

        assertEquals(1, result.size());
        assertEquals(invitation.getId(), result.getFirst().id());
    }

    // -------- getInvitationsByProject --------

    @Test
    void getInvitationsByProject_shouldThrow_whenProjectNotFound() {
        when(projectRepository.existsById(projectId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> invitationService.getInvitationsByProject(projectId));
    }

    @Test
    void getInvitationsByProject_shouldReturnList() {
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(invitationRepository.findAllByProjectId(projectId)).thenReturn(List.of(invitation));

        var result = invitationService.getInvitationsByProject(projectId);

        assertEquals(1, result.size());
        assertEquals(invitation.getId(), result.getFirst().id());
    }

    // -------- create --------

    @Test
    void create_shouldThrow_whenProjectNotFound() {
        var req = new InvitationCreateRequest(projectId, "invited@test.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> invitationService.create(req, authentication));
    }

    @Test
    void create_shouldThrow_whenInviterNotFound() {
        var req = new InvitationCreateRequest(projectId, "invited@test.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authentication.getName()).thenReturn("inviter@test.com");
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> invitationService.create(req, authentication));
    }

    @Test
    void create_shouldThrow_whenInvitedNotFound() {
        var req = new InvitationCreateRequest(projectId, "invited@test.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authentication.getName()).thenReturn("inviter@test.com");
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.of(inviter));
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> invitationService.create(req, authentication));
    }

    @Test
    void create_shouldThrow_whenInvitingSelf() {
        var req = new InvitationCreateRequest(projectId, "inviter@test.com");
        inviter.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        invited.setId(inviter.getId());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authentication.getName()).thenReturn("inviter@test.com");
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.of(inviter));
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.of(invited));

        assertThrows(ResponseStatusException.class,
                () -> invitationService.create(req, authentication));
    }

    @Test
    void create_shouldThrow_whenAlreadyMember() {
        var req = new InvitationCreateRequest(projectId, "invited@test.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authentication.getName()).thenReturn("inviter@test.com");
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.of(inviter));
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(projectMembershipService.isMember(projectId, invited.getId())).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> invitationService.create(req, authentication));
    }

    @Test
    void create_shouldThrow_whenPendingInvitationExists() {
        var req = new InvitationCreateRequest(projectId, "invited@test.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authentication.getName()).thenReturn("inviter@test.com");
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.of(inviter));
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(projectMembershipService.isMember(projectId, invited.getId())).thenReturn(false);
        when(invitationRepository.existsByProjectIdAndInvitedIdAndStatus(
                projectId, invited.getId(), Invitation.Status.PENDING)).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> invitationService.create(req, authentication));
    }

    @Test
    void create_shouldSaveInvitation() {
        var req = new InvitationCreateRequest(projectId, "invited@test.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authentication.getName()).thenReturn("inviter@test.com");
        when(userRepository.findByEmail("inviter@test.com")).thenReturn(Optional.of(inviter));
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(projectMembershipService.isMember(projectId, invited.getId())).thenReturn(false);
        when(invitationRepository.existsByProjectIdAndInvitedIdAndStatus(
                projectId, invited.getId(), Invitation.Status.PENDING)).thenReturn(false);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        Invitation result = invitationService.create(req, authentication);

        assertNotNull(result.getToken());
        assertEquals(Invitation.Status.PENDING, result.getStatus());
    }

    // -------- accept --------

    @Test
    void accept_shouldThrow_whenUserNotFound() {
        when(authentication.getName()).thenReturn("missing@test.com");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> invitationService.accept(invitationId, authentication));
    }

    @Test
    void accept_shouldThrow_whenInvitationNotFound() {
        when(authentication.getName()).thenReturn("invited@test.com");
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(invitationRepository.findPendingForUserWithProject(invitationId, invited.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> invitationService.accept(invitationId, authentication));
    }

    @Test
    void accept_shouldAcceptInvitation() {
        ProjectMembership pm = new ProjectMembership();
        pm.setId(UUID.randomUUID());
        pm.setRole(ProjectMembership.Role.OBSERVER);
        pm.setJoinedAt(Instant.now());

        when(authentication.getName()).thenReturn("invited@test.com");
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(invitationRepository.findPendingForUserWithProject(invitationId, invited.getId()))
                .thenReturn(Optional.of(invitation));
        when(projectMembershipService.createMembershipIfNotExists(project, invited, ProjectMembership.Role.OBSERVER))
                .thenReturn(pm);

        ProjectMemberResponse result = invitationService.accept(invitationId, authentication);

        assertEquals(pm.getId(), result.membershipId());
        verify(invitationRepository).delete(invitation);
    }

    // -------- reject --------

    @Test
    void reject_shouldThrow_whenUserNotFound() {
        when(authentication.getName()).thenReturn("missing@test.com");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> invitationService.reject(invitationId, authentication));
    }

    @Test
    void reject_shouldThrow_whenInvitationNotFound() {
        when(authentication.getName()).thenReturn("invited@test.com");
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(invitationRepository.findPendingForUserWithProject(invitationId, invited.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> invitationService.reject(invitationId, authentication));
    }

    @Test
    void reject_shouldDeleteInvitation() {
        when(authentication.getName()).thenReturn("invited@test.com");
        when(userRepository.findByEmail("invited@test.com")).thenReturn(Optional.of(invited));
        when(invitationRepository.findPendingForUserWithProject(invitationId, invited.getId()))
                .thenReturn(Optional.of(invitation));

        invitationService.reject(invitationId, authentication);

        verify(invitationRepository).delete(invitation);
    }
}
