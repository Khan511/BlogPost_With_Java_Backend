package com.example.demo.utils;

import static com.example.demo.constant.Constant.NAJI;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;

public class UserUtils {

    public static Supplier<String> qrCodeSecret = () -> new DefaultSecretGenerator().generate();

    public static BiFunction<String, String, QrData> qrDataFunction = (email, qrCodeSecret) -> new QrData.Builder()
            .issuer(NAJI)
            .label(email)
            .secret(qrCodeSecret)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();

    public static BiFunction<String, String, String> qrCodeImageUri = (email, qrCodeSecret) -> {

        var data = qrDataFunction.apply(email, qrCodeSecret);
        var generator = new ZxingPngQrGenerator();
        byte[] imageData;

        try {
            imageData = generator.generate(data);
        } catch (Exception e) {
            throw new RuntimeException("Uable to create QR code URI");
        }
        return getDataUriForImage(imageData, generator.getImageMimeType());
    };
}