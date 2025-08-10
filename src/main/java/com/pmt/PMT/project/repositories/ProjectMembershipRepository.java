package com.pmt.PMT.project.repositories;

import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {

    List<ProjectMembership> findByProject(Project project);

    List<ProjectMembership> findByUser(User user);

    List<ProjectMembership> findByProjectAndRole(Project project, ProjectMembership.Role role);
}
