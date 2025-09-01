package com.pmt.PMT.project.repositories;

import com.pmt.PMT.project.models.Invitation;
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
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    List<Invitation> findAllByProjectId(UUID projectId);

    List<Invitation> findByInvitedOrderByCreatedAtDesc(User user);

    // InvitationRepository.java
    @Query("""
       select i from Invitation i
       join fetch i.project p
       join fetch i.invited u
       where i.id = :id and u.id = :userId and i.status = 'PENDING'
       """)
    Optional<Invitation> findPendingForUserWithProject(@Param("id") UUID id, @Param("userId") UUID userId);

    boolean existsByProjectIdAndInvitedIdAndStatus(UUID projectId, UUID invitedId, Invitation.Status status);

}
