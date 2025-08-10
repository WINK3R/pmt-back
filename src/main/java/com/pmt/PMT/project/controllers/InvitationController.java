package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.models.Invitation;
import com.pmt.PMT.project.services.InvitationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Invitations")
@RestController
@RequestMapping("/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @GetMapping
    public List<Invitation> getAll() {
        return invitationService.findAll();
    }

    @GetMapping("/{id}")
    public Invitation getById(@PathVariable UUID id) {
        return invitationService.getById(id);
    }

    @GetMapping("/by-token/{token}")
    public Invitation getByToken(@PathVariable String token) {
        return invitationService.getByToken(token);
    }

    @PostMapping
    public Invitation create(@RequestBody Invitation invitation) {
        return invitationService.create(invitation);
    }
}
