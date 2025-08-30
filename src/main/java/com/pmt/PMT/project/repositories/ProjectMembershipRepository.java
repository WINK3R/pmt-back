package com.pmt.PMT.project.repositories;

import com.pmt.PMT.project.models.ProjectMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {

    @Query("""
           select pm from ProjectMembership pm
           join fetch pm.user u
           where pm.project.id = :projectId
           order by pm.joinedAt asc
           """)
    List<ProjectMembership> findByProjectIdWithUser(@Param("projectId") UUID projectId);

    @Query("""
           select pm from ProjectMembership pm
           join fetch pm.user u
           where pm.project.id = :projectId and pm.role = :role
           order by pm.joinedAt asc
           """)
    List<ProjectMembership> findByProjectIdAndRoleWithUser(@Param("projectId") UUID projectId,
                                                           @Param("role") ProjectMembership.Role role);
}