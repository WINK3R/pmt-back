package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.*;
import com.pmt.PMT.project.mappers.InvitationMapper;
import com.pmt.PMT.project.mappers.UserMapper;
import com.pmt.PMT.project.models.*;
import com.pmt.PMT.project.repositories.InvitationRepository;
import com.pmt.PMT.project.repositories.ProjectRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectMembershipService projectMembershipService;

    @Transactional
    public List<InvitationResponse> listByUser(Authentication auth) {
        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return invitationRepository.findByInvitedOrderByCreatedAtDesc(user)
                .stream()
                .map(InvitationMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<InvitationResponse> getInvitationsByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found: " + projectId);
        }
        List<Invitation> list = invitationRepository
                .findAllByProjectId(projectId);
        return list.stream().map(InvitationMapper::toResponse).toList();
    }


    public Invitation create(InvitationCreateRequest request, Authentication auth) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User inviter = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Inviter not found: " + auth.getName()));
        User invited = userRepository.findByEmail(request.emailInvited())
                .orElseThrow(() -> new UsernameNotFoundException("Inviter not found: " + request.emailInvited()));

        // Prevent inviting yourself
        if (inviter.getId().equals(invited.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot invite yourself to the project.");
        }
        // Already a member?
        if (projectMembershipService.isMember(project.getId(), invited.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this project.");
        }
        // Pending invite already exists?
        if (invitationRepository.existsByProjectIdAndInvitedIdAndStatus(
                project.getId(), invited.getId(), Invitation.Status.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending invitation already exists for this user.");
        }

        Invitation invitation = new Invitation();
        invitation.setProject(project);
        invitation.setInviter(inviter);
        invitation.setInvited(invited);
        invitation.setStatus(Invitation.Status.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setCreatedAt(Instant.now());

        return invitationRepository.save(invitation);
    }

    @Transactional
    public ProjectMemberResponse accept(UUID invitationId, Authentication auth) {
        User current = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Invitation inv = invitationRepository.findPendingForUserWithProject(invitationId, current.getId())
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found or not accessible"));

        ProjectMembership pm = projectMembershipService
                .createMembershipIfNotExists(inv.getProject(), current, ProjectMembership.Role.OBSERVER);

        inv.setStatus(Invitation.Status.ACCEPTED);
        inv.setAcceptedAt(Instant.now());
        invitationRepository.delete(inv);

        return new ProjectMemberResponse(
                pm.getId(),
                pm.getRole(),
                pm.getJoinedAt(),
                UserMapper.toSummary(current)
        );
    }

    @Transactional
    public void reject(UUID invitationId, Authentication auth) {
        User current = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Invitation inv = invitationRepository.findPendingForUserWithProject(invitationId, current.getId())
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found or not accessible"));

        invitationRepository.delete(inv);
    }

}
