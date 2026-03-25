package com.example.learningplan.controller;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class ControllerImageSupport {
    private ControllerImageSupport() {}

    static List<String> parseImages(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    static List<String> mergeImages(List<String> first, List<String> second) {
        List<String> merged = new ArrayList<>();
        if (first != null) {
            merged.addAll(first);
        }
        if (second != null) {
            merged.addAll(second);
        }
        return merged;
    }

    static String validateOptionalImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return validateRequiredImage(file);
    }

    static String validateRequiredImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "Please select an image file";
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            return "Only JPG and PNG images are supported";
        }
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();
            if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
                return "Only JPG and PNG images are supported";
            }
        }
        if (!isImageMagic(file)) {
            return "Invalid image file content";
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            return "Image size must be no more than 10MB";
        }
        return null;
    }

    private static boolean isImageMagic(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = in.read(header);
            if (read < 3) {
                return false;
            }
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }
            return read >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
        }
    }
}
