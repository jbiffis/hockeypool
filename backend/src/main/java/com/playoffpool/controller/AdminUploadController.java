package com.playoffpool.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin")
public class AdminUploadController {

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    @GetMapping("/uploads")
    public ResponseEntity<List<Map<String, Object>>> listUploads() throws IOException {
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            return ResponseEntity.ok(List.of());
        }
        try (Stream<Path> files = Files.list(dirPath)) {
            List<Map<String, Object>> result = files
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparingLong(p -> {
                    try { return Files.getLastModifiedTime(p).toMillis(); } catch (IOException e) { return 0L; }
                }))
                .map(p -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("filename", p.getFileName().toString());
                    entry.put("url", "/api/uploads/" + p.getFileName().toString());
                    try { entry.put("size", Files.size(p)); } catch (IOException e) { entry.put("size", 0); }
                    return entry;
                })
                .collect(Collectors.toList());
            Collections.reverse(result);
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.matches("\\.[a-z0-9]+")) {
                extension = ext;
            }
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);

        Path filePath = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        File savedFile = filePath.toFile();
        savedFile.setExecutable(false, false);
        savedFile.setReadable(true, false);
        savedFile.setWritable(true, true);

        return ResponseEntity.ok(Map.of("url", "/api/uploads/" + filename));
    }
}
