package com.example.demo.awsconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// Central configuration for AWS S3 connectivity
// Creates reusable beans for S3 operations
@Configuration
public class S3Config {
        @Value("${aws.accessKeyId}")
        private String accessKey;

        @Value("${aws.secretKey}")
        private String secretkey;

        @Value("${aws.s3.bucket}")
        private String buckteName;

        // Bean for S3Presigner to generate pre-signed URLs
        // Used to generate pre-signed URLs for secure temporary access
        // Required for secure uploads/downloads without exposing credentials
        // Region must match the bucket's actual regio
        @Bean
        public S3Presigner s3Presigner() {
                return S3Presigner.builder()
                                // Frankfurt region
                                .region(Region.EU_CENTRAL_1)
                                // Configure credentials using access key and secret key
                                .credentialsProvider(
                                                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey,
                                                                secretkey)))
                                .build();
        }

        // Bean for S3Client to perform direct S3 operations
        // Main client for direct S3 operations (upload/delete/list objects)
        // Used for non-presigned operations like direct deletions
        // Must match presigner's region configuration
        @Bean
        public S3Client s3Client() {
                return S3Client.builder()
                                // Must match presigner's region
                                .region(Region.EU_CENTRAL_1)
                                // Same credentials as presigner for consistency
                                .credentialsProvider(
                                                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey,
                                                                secretkey)))
                                .build();
        }
}
