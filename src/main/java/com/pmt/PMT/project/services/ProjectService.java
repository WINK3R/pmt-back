package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.ProjectCreateRequest;
import com.pmt.PMT.project.dto.ProjectListItem;
import com.pmt.PMT.project.dto.ProjectResponse;
import com.pmt.PMT.project.dto.UserSummary;
import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.Task;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.ProjectRepository;
import com.pmt.PMT.project.repositories.TaskRepository;
import com.pmt.PMT.project.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectMembershipService projectMembershipService;

    @Autowired
    private TaskRepository taskRepository;

    public List<ProjectListItem> list() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getByIdForMember(UUID id, Authentication auth) {
        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var p = projectRepository.findWithCreatedBy(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (projectMembershipService
                .getMemberResponsesByProjectId(id)
                .stream().noneMatch(m -> m.user().id().equals(user.getId()))) {
            throw new EntityNotFoundException("Membership not found");
        }
        return toDetail(p);
    }

    @Transactional
    public List<ProjectListItem> listByUser(Authentication auth) {
        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return projectRepository.findByMemberOrderByCreatedAtDesc(user)
                .stream()
                .map(p -> {
                    long total = taskRepository.countByProjectId(p.getId());
                    long completed = taskRepository.countByProjectIdAndStatus(p.getId(), Task.Status.COMPLETED);
                    long open = Math.max(0, total - completed);
                    return new ProjectListItem(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getTag(),
                            p.getStartDate(),
                            p.getCreatedAt(),
                            new UserSummary(
                                    p.getCreatedBy().getId(),
                                    p.getCreatedBy().getUsername(),
                                    p.getCreatedBy().getProfileImageUrl()
                            ),
                            open,
                            completed
                    );
                })
                .toList();
    }

    @Transactional
    public ProjectListItem create(ProjectCreateRequest req, Authentication auth) {
        User creator = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + auth.getName()));

        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setStartDate(req.startDate());
        p.setTag(req.tag());
        p.setCreatedBy(creator);

        Project saved = projectRepository.save(p);
        projectMembershipService.createMembership(p, creator, ProjectMembership.Role.OWNER);
        return toResponse(saved);
    }

    private ProjectListItem toResponse(Project p) {
        long total = taskRepository.countByProjectId(p.getId());
        long completed = taskRepository.countByProjectIdAndStatus(p.getId(), Task.Status.COMPLETED);
        long open = Math.max(0, total - completed);
        return new ProjectListItem(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getTag(),
                p.getStartDate(),
                p.getCreatedAt(),
                new UserSummary(
                        p.getCreatedBy().getId(),
                        p.getCreatedBy().getUsername(),
                        p.getCreatedBy().getProfileImageUrl()
                ),
                open,
                completed
        );
    }

    private ProjectResponse toDetail(Project p) {
        User u = p.getCreatedBy();
        UserSummary summary = new UserSummary(u.getId(), u.getEmail(), u.getUsername());
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getTag(),
                p.getStartDate(),
                p.getCreatedAt(),
                summary
        );
    }
}
