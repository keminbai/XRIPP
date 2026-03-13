package com.xripp.backend.controller.v3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonV3Controller {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "mp4", "mov", "avi",
            "zip", "rar", "7z"
    );

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return errorResult("file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return errorResult("file size exceeds 50MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return errorResult("filename is empty");
        }

        String ext = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return errorResult("file type not allowed: " + ext);
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String safeFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

            Path uploadDir = Paths.get("./uploads", datePath);
            Files.createDirectories(uploadDir);

            Path targetPath = uploadDir.resolve(safeFilename);
            file.transferTo(targetPath.toFile());

            String url = "/uploads/" + datePath + "/" + safeFilename;
            log.info("[Upload] saved: {} -> {} ({}bytes)", originalFilename, url, file.getSize());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("url", url);
            data.put("fileName", originalFilename);
            data.put("storedName", safeFilename);
            data.put("fileExt", ext);
            data.put("fileSize", file.getSize());
            data.put("contentType", safe(file.getContentType()));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
            return result;

        } catch (IOException e) {
            log.error("[Upload] failed: {}", e.getMessage(), e);
            return errorResult("upload failed: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    private Map<String, Object> errorResult(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 400);
        result.put("message", message);
        result.put("data", null);
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
