package com.pmt.PMT.project.controllers;

import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.UserRepository;
import com.pmt.PMT.project.security.JwtService;
import com.pmt.PMT.project.storage.LocalAvatarStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvatarController.class)
@AutoConfigureMockMvc(addFilters = false) // pas besoin de sécurité ici
class AvatarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private UserRepository userRepository;
    @MockitoBean private LocalAvatarStorage storage;
    @MockitoBean private JwtService jwtService;

    @Test
    void uploadAvatar_shouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        // Mock repo
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock storage
        var stored = new LocalAvatarStorage.StoredFile("avatars/" + userId + ".png", "/uploads/avatars/" + userId + ".png");
        when(storage.store(any(), any())).thenReturn(stored);

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/users/{id}/avatar", userId).file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/uploads/avatars/" + userId + ".png"));
    }

    @Test
    void uploadAvatar_shouldReturnNotFound_whenUserMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());

        mockMvc.perform(multipart("/users/{id}/avatar", userId).file(file).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadAvatar_shouldReturnBadRequest_onIllegalArgument() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(storage.store(any(), any())).thenThrow(new IllegalArgumentException("Invalid file"));

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "bad".getBytes());

        mockMvc.perform(multipart("/users/{id}/avatar", userId).file(file).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid file"));
    }

    @Test
    void uploadAvatar_shouldReturnInternalServerError_onOtherException() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(storage.store(any(), any())).thenThrow(new RuntimeException("Boom"));

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "boom".getBytes());

        mockMvc.perform(multipart("/users/{id}/avatar", userId).file(file).with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Upload failed"));
    }
}
