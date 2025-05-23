package com.example.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.PresignedUrlRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3Controller(S3Client s3Client, S3Presigner s3Presigner, @Value("${aws.s3.bucket}") String buckteName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = buckteName;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<?> generatePresignedUrl(@RequestBody PresignedUrlRequest request) {

        // Validate the file name
        if (request.getFileName().contains("..") || request.getFileName().contains("/")) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }

        String folder = switch (request.getFolderType()) {
            case "blog-image" -> "blog-images/";
            default -> "profile-images/";
        };

        String objectKey = folder + UUID.randomUUID() + "_" + request.getFileName();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getFileType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        // return ResponseEntity.ok(Map.of("presignedUrl",
        // presignedRequest.url().toString(), "fileUrl",
        // "https://dwy0mahvkrvvq.cloudfront.net/" + objectKey));

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        String fileUrl;
        try {

            fileUrl = new URI("https", "dwy0mahvkrvvq.cloudfront.net", "/" + objectKey, null).toString();

        } catch (URISyntaxException e) {
            throw new RuntimeException("Error contructing file URL", e);
        }

        return ResponseEntity.ok(Map.of("presignedUrl", presignedRequest.url().toString(), "fileUrl", fileUrl));
        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    }

    @DeleteMapping("/delete-object")
    public ResponseEntity<?> deleteObject(@RequestBody Map<String, String> request) {
        try {

            String imageUrl = request.get("imageUrl");
            System.out.println("S3Controller Image Url: " + imageUrl);
            String objectKey = extractKeyFromUrl(imageUrl);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
            return ResponseEntity.ok().build();
        } catch (S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete object: " + e.getMessage());
        }
    }

    private String extractKeyFromUrl(String imageUrl) {
        try {
            // Handle URL-encded chracters
            // Parse the URI first to get the path
            URI uri = new URI(imageUrl);
            String path = uri.getPath();

            // Remove leading slash if present
            String key = path.startsWith("/") ? path.substring(1) : path;

            // Decode URL-encoded characters(like %20 -> space)
            return URLDecoder.decode(key, StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid image URL format");
        }
    }
    // private String extractKeyFromUrl(String imageUrl) {
    // try {
    // // Handle URL-encded chracters
    // String decodeUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
    // URI uri = new URI(decodeUrl);
    // String path = uri.getPath();
    // return path.startsWith("/") ? path.substring(1) : path;
    // } catch (URISyntaxException e) {
    // throw new IllegalArgumentException("Invalid image URL format");
    // }
    // }

}
