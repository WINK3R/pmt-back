package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.Invitation;
import com.pmt.PMT.project.repositories.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    public List<Invitation> findAll() {
        return invitationRepository.findAll();
    }

    public Invitation getById(UUID id) {
        return invitationRepository.findById(id).orElseThrow();
    }

    public Invitation create(Invitation invitation) {
        return invitationRepository.save(invitation);
    }

    public Invitation getByToken(String token) {
        return invitationRepository.findByToken(token).orElseThrow();
    }
}
