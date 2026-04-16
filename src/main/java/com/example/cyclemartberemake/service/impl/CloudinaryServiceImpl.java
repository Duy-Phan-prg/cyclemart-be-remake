package com.example.cyclemartberemake.service.impl;

import com.cloudinary.Cloudinary;
import com.example.cyclemartberemake.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file) {
        try {
            // TODO: Replace with real Cloudinary config
            // For now, return a mock URL for testing
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            // Mock URL for testing - replace with real Cloudinary upload
            return "https://res.cloudinary.com/demo/image/upload/v1234567890/cyclemart/" + file.getOriginalFilename();
            
            /* Real Cloudinary code - uncomment when config is ready:
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "cyclemart")
            );

            return uploadResult.get("secure_url").toString();
            */

        } catch (Exception e) {
            throw new RuntimeException("Upload fail: " + e.getMessage());
        }
    }

    @Override
    public void delete(String url) {
        try {
            // lấy public_id từ URL
            String publicId = extractPublicId(url);

            cloudinary.uploader().destroy(publicId, Map.of());

        } catch (Exception e) {
            throw new RuntimeException("Delete fail");
        }
    }

    private String extractPublicId(String url) {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];

        return "cyclemart/" + fileName.substring(0, fileName.lastIndexOf("."));
    }
}