package com.pmt.PMT.project.repositories;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("""
        select distinct p
        from Project p
        join fetch p.createdBy
        where exists (
            select 1 from ProjectMembership pm
            where pm.project = p and pm.user = :user
        )
        order by p.createdAt desc
    """)
    List<Project> findByMemberOrderByCreatedAtDesc(@Param("user") User user);

    @Query("""
        select p from Project p
        join fetch p.createdBy
        where p.id = :id
    """)
    Optional<Project> findWithCreatedBy(@Param("id") UUID id);

    List<Project> findByCreatedByOrderByCreatedAtDesc(User user);
    }

