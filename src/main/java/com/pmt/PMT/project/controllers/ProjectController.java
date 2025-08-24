package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.ProjectCreateRequest;
import com.pmt.PMT.project.dto.ProjectResponse;
import com.pmt.PMT.project.dto.TaskResponse;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.services.ProjectMembershipService;
import com.pmt.PMT.project.services.ProjectService;
import com.pmt.PMT.project.services.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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

    @GetMapping
    public Object getAll(Authentication auth) {
        return projectService.listByUser(auth);
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectService.getById(id);
    }
    @PostMapping
    public ProjectResponse create(@RequestBody ProjectCreateRequest body, Authentication auth) {
        return projectService.create(body, auth);
    }

    @GetMapping("/{projectId}/members")
    public List<ProjectMembership> getMembers(@PathVariable UUID projectId) {
        Project project = new Project();
        project.setId(projectId);
        return projectMembershipService.getByProject(project);
    }

    @GetMapping("/{projectId}/members/by-role/{role}")
    public List<ProjectMembership> getMembersByRole(@PathVariable UUID projectId,
                                                    @PathVariable ProjectMembership.Role role) {
        Project project = new Project();
        project.setId(projectId);
        return projectMembershipService.getByProjectAndRole(project, role);
    }

    @PostMapping("/{projectId}/members")
    public ProjectMembership addMember(@PathVariable UUID projectId,
                                       @RequestBody ProjectMembership membership) {
        Project project = new Project();
        project.setId(projectId);
        membership.setProject(project);

        if (membership.getJoinedAt() == null) membership.setJoinedAt(Instant.now());

        return projectMembershipService.create(membership);
    }

    @DeleteMapping("/{projectId}/members/{membershipId}")
    public void removeMember(@PathVariable UUID projectId,
                             @PathVariable UUID membershipId) {
        projectMembershipService.delete(membershipId);
    }

    @GetMapping("/{projectId}/tasks")
    public List<TaskResponse> getTasksByProject(@PathVariable UUID projectId) {
        return taskService.getByProjectId(projectId);
    }
}
