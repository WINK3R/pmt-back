package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.*;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.services.InvitationService;
import com.pmt.PMT.project.services.ProjectMembershipService;
import com.pmt.PMT.project.services.ProjectService;
import com.pmt.PMT.project.services.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Projects")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMembershipService projectMembershipService;

    @Autowired
    private TaskService taskService;
    @Autowired
    private InvitationService invitationService;

    @GetMapping
    public Object getAll(Authentication auth) {
        return projectService.listByUser(auth);
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id, Authentication auth) {
        return projectService.getByIdForMember(id, auth);
    }
    @PostMapping
    public ProjectListItem create(@RequestBody ProjectCreateRequest body, Authentication auth) {
        return projectService.create(body, auth);
    }

    @GetMapping("/{projectId}/tasks")
    public List<TaskResponse> getTasksByProject(@PathVariable UUID projectId) {
        return taskService.getByProjectId(projectId);
    }

    @GetMapping("/{projectId}/members")
    public List<ProjectMemberResponse> getMembers(@PathVariable UUID projectId) {
        return projectMembershipService.getMemberResponsesByProjectId(projectId);
    }

    @GetMapping("/{projectId}/invitations")
    public List<InvitationResponse> getInvitations(@PathVariable UUID projectId) {
        return invitationService.getInvitationsByProject(projectId);
    }

    @GetMapping("/{projectId}/members/by-role/{role}")
    public List<ProjectMemberResponse> getMembersByRole(@PathVariable UUID projectId,
                                                        @PathVariable ProjectMembership.Role role) {
        return projectMembershipService.getMemberResponsesByProjectIdAndRole(projectId, role);
    }

    @PostMapping("/{projectId}/members")
    public ProjectMembership addMember(
            @PathVariable UUID projectId,
            @RequestParam UUID userId,
            @RequestParam ProjectMembership.Role role
    ) {
        Project project = new Project();
        project.setId(projectId);

        User user = new User();
        user.setId(userId);

        return projectMembershipService.createMembership(project, user, role);
    }

    @DeleteMapping("/{projectId}/members/{membershipId}")
    public void removeMember(@PathVariable UUID projectId,
                             @PathVariable UUID membershipId) {
        projectMembershipService.delete(membershipId);
    }

    @PutMapping("/{projectId}/members/{membershipId}/role")
    public ProjectMemberResponse changeMemberRole(@PathVariable UUID projectId,
                                                  @PathVariable UUID membershipId,
                                                  @RequestBody RoleUpdateRequest body) {
        return projectMembershipService.changeRole(projectId, membershipId, body.role);
    }


}
