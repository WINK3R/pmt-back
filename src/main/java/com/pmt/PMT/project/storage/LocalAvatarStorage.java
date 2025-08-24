package com.pmt.PMT.project.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalAvatarStorage {

    private final Path root = Paths.get("uploads/avatars");

    public LocalAvatarStorage() throws IOException {
        Files.createDirectories(root);
    }

    public StoredFile store(MultipartFile file, UUID userId) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String ext = getExtension(file.getOriginalFilename());
        String filename = userId + "-" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        Path dest = root.resolve(filename).normalize();
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/uploads/avatars/" + filename;

        return new StoredFile(filename, publicUrl);
    }

    public void deleteIfLocal(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;
        try {
            Path p = root.resolve(Paths.get(storageKey).getFileName().toString());
            Files.deleteIfExists(p);
        } catch (Exception ignored) {}
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i + 1) : "";
    }

    public record StoredFile(String key, String publicUrl) {}
}
