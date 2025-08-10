package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.Project;
import com.pmt.PMT.project.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project create(Project project) {
        return projectRepository.save(project);
    }

    public Project getById(UUID id) {
        return projectRepository.findById(id).orElseThrow();
    }
}
