package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.*;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.security.JwtService;
import com.pmt.PMT.project.services.InvitationService;
import com.pmt.PMT.project.services.ProjectMembershipService;
import com.pmt.PMT.project.services.ProjectService;
import com.pmt.PMT.project.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = true)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProjectService projectService;
    @MockitoBean private ProjectMembershipService projectMembershipService;
    @MockitoBean private TaskService taskService;
    @MockitoBean private InvitationService invitationService;
    @MockitoBean private JwtService jwtService;

    @Test
    @WithMockUser
    void getAll_shouldReturnProjects() throws Exception {
        when(projectService.listByUser(any())).thenReturn(List.of());
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProjectById_shouldReturnProjectResponse() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(
                projectId, "Test", "Desc", "tag", LocalDate.now(), Instant.now(), null
        );
        when(projectService.getByIdForMember(eq(projectId), any())).thenReturn(response);

        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @WithMockUser
    void create_shouldReturnProjectListItem() throws Exception {
        ProjectListItem item = new ProjectListItem(
                UUID.randomUUID(), "New Project", "Desc", "tag",
                LocalDate.now(), Instant.now(), null, 0, 0
        );
        when(projectService.create(any(), any())).thenReturn(item);

        mockMvc.perform(post("/projects")
                        .contentType("application/json")
                        .content("""
                        {"name":"New Project","description":"Desc","tag":"tag"}
                        """).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    @WithMockUser
    void getTasksByProject_shouldReturnTasks() throws Exception {
        when(taskService.getByProjectId(any())).thenReturn(List.of());
        mockMvc.perform(get("/projects/{id}/tasks", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMembers_shouldReturnMembers() throws Exception {
        when(projectMembershipService.getMemberResponsesByProjectId(any())).thenReturn(List.of());
        mockMvc.perform(get("/projects/{id}/members", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getInvitations_shouldReturnInvitations() throws Exception {
        when(invitationService.getInvitationsByProject(any())).thenReturn(List.of());
        mockMvc.perform(get("/projects/{id}/invitations", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMembersByRole_shouldReturnFilteredMembers() throws Exception {
        when(projectMembershipService.getMemberResponsesByProjectIdAndRole(any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/projects/{id}/members/by-role/OWNER", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addMember_shouldReturnMembership() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProjectMembership membership = new ProjectMembership();
        membership.setId(UUID.randomUUID());
        membership.setJoinedAt(Instant.now());
        membership.setRole(ProjectMembership.Role.OWNER);

        when(projectMembershipService.createMembership(any(Project.class), any(User.class), any(ProjectMembership.Role.class)))
                .thenReturn(membership);

        mockMvc.perform(post("/projects/{projectId}/members", projectId)
                        .param("userId", userId.toString())
                        .param("role", "OWNER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(membership.getId().toString()))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    @WithMockUser
    void removeMember_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/projects/{id}/members/{m}", UUID.randomUUID(), UUID.randomUUID()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changeMemberRole_shouldReturnUpdatedMember() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant joinedAt = Instant.now();

        UserSummary userSummary = new UserSummary(userId, "alice", "avatar.png");
        ProjectMemberResponse updated = new ProjectMemberResponse(
                membershipId,
                ProjectMembership.Role.MEMBER,
                joinedAt,
                userSummary
        );

        when(projectMembershipService.changeRole(eq(projectId), eq(membershipId), any()))
                .thenReturn(updated);

        mockMvc.perform(put("/projects/{projectId}/members/{membershipId}/role", projectId, membershipId)
                        .contentType("application/json")
                        .content("{\"role\":\"MEMBER\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipId").value(membershipId.toString()))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.joinedAt").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.username").value("alice"))
                .andExpect(jsonPath("$.user.profileImageUrl").value("avatar.png"));
    }

}
