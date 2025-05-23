package com.example.demo.dto;

import lombok.Data;

@Data
public class PresignedUrlRequest {

    private String fileName;
    private String fileType;
    private String folderType;

}
