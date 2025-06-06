package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrCodeRequest {

    private String userId;
    @NotEmpty(message = "QR code cannot be empty or null")
    private String qrCode;

}
