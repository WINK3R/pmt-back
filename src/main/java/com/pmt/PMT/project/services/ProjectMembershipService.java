package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.ProjectMemberResponse;
import com.pmt.PMT.project.mappers.UserMapper;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.ProjectMembershipRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.pmt.PMT.project.mappers.InvitationMapper.toResponse;

@Service
public class ProjectMembershipService {

    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;
    @Autowired
    private ProjectMembershipRepository membershipRepo;

    public List<ProjectMembership> findAll() {
        return projectMembershipRepository.findAll();
    }

    public ProjectMembership getById(UUID id) {
        return projectMembershipRepository.findById(id).orElseThrow();
    }

    @Transactional
    public ProjectMembership createMembership(Project project, User user, ProjectMembership.Role role) {
        ProjectMembership pm = new ProjectMembership();
        pm.setProject(project);
        pm.setUser(user);
        pm.setRole(role);
        pm.setJoinedAt(Instant.now());
        return projectMembershipRepository.save(pm);
    }

    public void delete(UUID id) {
        projectMembershipRepository.deleteById(id);
    }

    @Transactional
    public List<ProjectMemberResponse> getMemberResponsesByProjectId(UUID projectId) {
        return projectMembershipRepository.findByProjectIdWithUser(projectId)
                .stream()
                .map(pm -> new ProjectMemberResponse(
                        pm.getId(),
                        pm.getRole(),
                        pm.getJoinedAt(),
                        UserMapper.toSummary(pm.getUser())
                ))
                .toList();
    }

    @Transactional
    public List<ProjectMemberResponse> getMemberResponsesByProjectIdAndRole(UUID projectId,
                                                                            ProjectMembership.Role role) {
        return projectMembershipRepository.findByProjectIdAndRoleWithUser(projectId, role)
                .stream()
                .map(pm -> new ProjectMemberResponse(
                        pm.getId(),
                        pm.getRole(),
                        pm.getJoinedAt(),
                        UserMapper.toSummary(pm.getUser())
                ))
                .toList();
    }

    @Transactional
    public ProjectMembership createMembershipIfNotExists(Project project, User user, ProjectMembership.Role role) {
        if (projectMembershipRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
            ProjectMembership pm = new ProjectMembership();
            pm.setProject(project);
            pm.setUser(user);
            pm.setRole(role);
            pm.setJoinedAt(Instant.now());
            return pm;
        }
        return createMembership(project, user, role);
    }

    @Transactional
    public boolean isMember(UUID projectId, UUID userId) {
        return projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Transactional
    public ProjectMemberResponse changeRole(UUID projectId, UUID membershipId, ProjectMembership.Role newRole) {
        ProjectMembership membership = projectMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));

        if (!membership.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership does not belong to project");
        }

        membership.setRole(newRole);
        projectMembershipRepository.save(membership);

        return new ProjectMemberResponse(membership.getId(), membership.getRole(), membership.getJoinedAt(), UserMapper.toSummary(membership.getUser()));
    }


}
