package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.dto.InvitationCreateRequest;
import com.pmt.PMT.project.dto.InvitationResponse;
import com.pmt.PMT.project.dto.ProjectCreateRequest;
import com.pmt.PMT.project.dto.ProjectMemberResponse;
import com.pmt.PMT.project.mappers.InvitationMapper;
import com.pmt.PMT.project.models.Invitation;
import com.pmt.PMT.project.services.InvitationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    public Object getAll(Authentication auth) {
        return invitationService.listByUser(auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationResponse create(@Valid @RequestBody InvitationCreateRequest request, Authentication auth) {
        Invitation inv = invitationService.create(request, auth);
        return InvitationMapper.toResponse(inv);
    }

    // InvitationController.java
    @PostMapping("/{id}/accept")
    public ProjectMemberResponse accept(@PathVariable UUID id, Authentication auth) {
        return invitationService.accept(id, auth);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable UUID id, Authentication auth) {
        invitationService.reject(id, auth);
    }

}
