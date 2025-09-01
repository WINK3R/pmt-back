package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.UserRepository;
import com.pmt.PMT.project.storage.LocalAvatarStorage;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class AvatarController {

    private final UserRepository userRepository;
    private final LocalAvatarStorage storage;

    public AvatarController(UserRepository userRepository, LocalAvatarStorage storage) {
        this.userRepository = userRepository;
        this.storage = storage;
    }

    @PostMapping(path = "/{id}/avatar", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<?> uploadAvatar(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        try {
            // Delete old if it was a previous upload (don’t delete default)
            if (user.getProfileImageKey() != null && !user.getProfileImageKey().equals("avatars/default.png")) {
                storage.deleteIfLocal(user.getProfileImageKey());
            }

            var stored = storage.store(file, user.getId());
            user.setProfileImageKey(stored.key());      // e.g., "3d…-….png"
            user.setProfileImageUrl(stored.publicUrl()); // e.g., "/uploads/avatars/3d…-….png"

            userRepository.save(user);
            return ResponseEntity.ok().body(new AvatarResponse(user.getProfileImageUrl()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Upload failed");
        }
    }

    public record AvatarResponse(String url) {}
}
