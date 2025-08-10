package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.ProjectMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectMembershipService {

    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;

    public List<ProjectMembership> findAll() {
        return projectMembershipRepository.findAll();
    }

    public ProjectMembership getById(UUID id) {
        return projectMembershipRepository.findById(id).orElseThrow();
    }

    public ProjectMembership create(ProjectMembership membership) {
        return projectMembershipRepository.save(membership);
    }

    public List<ProjectMembership> getByProject(Project project) {
        return projectMembershipRepository.findByProject(project);
    }

    public List<ProjectMembership> getByUser(User user) {
        return projectMembershipRepository.findByUser(user);
    }

    public List<ProjectMembership> getByProjectAndRole(Project project, ProjectMembership.Role role) {
        return projectMembershipRepository.findByProjectAndRole(project, role);
    }

    public void delete(UUID id) {
        projectMembershipRepository.deleteById(id);
    }
}
