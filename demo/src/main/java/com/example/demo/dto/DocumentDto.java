package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {

    private String uri;
    private String name;
    private String type;
    private Long size;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime ownerLastLogin;
    private String updaterName;
    private Long id;
    private String formatteSize;

    // // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // // SAVING THE DOCUMENTS IN COMPUTERS
    // private Long id;
    // private String documentId;
    // private String name;
    // private String description;
    // private String uri;
    // private Long size;
    // private String formatteSize;
    // private String icon;
    // private String extension;
    // private String referenceId;
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
    // private String ownerName;
    // private String ownerEmail;
    // private LocalDateTime ownerLastLogin;
    // private String updaterName;

}
