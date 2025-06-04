package com.example.demo.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.example.demo.dto.DocumentDto;
import com.example.demo.entities.DocumentEntity;
import com.example.demo.entities.UserEntity;

public class DocumentUtils {

    public static DocumentDto toDocumentDto(UserEntity user, DocumentEntity documentEntity) {

        DocumentDto documentDto = new DocumentDto();

        BeanUtils.copyProperties(documentEntity, documentDto);
        documentDto.setOwnerName(documentEntity.getOwner().getUsername());
        documentDto.setOwnerEmail(documentEntity.getOwner().getEmail());
        documentDto.setOwnerLastLogin(documentEntity.getOwner().getLastLogin());
        documentDto.setUpdaterName(user.getUsername());

        return documentDto;
    }

    public static String getDocumentUri(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(String.format("/documents/%s/%%", filename))
                .toUriString();
    }

    public static String setIcon(String fileExtension) {
        String extension = StringUtils.trimAllWhitespace(fileExtension);

        if (extension.equalsIgnoreCase("DOC") || extension.equalsIgnoreCase("DOCX")) {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/word-icon.svg";
        }
        if (extension.equalsIgnoreCase("XLS") || extension.equalsIgnoreCase("XLSX")) {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/excel-icon.svg";
        }
        if (extension.equalsIgnoreCase("PDF")) {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/pdf-icon.svg";
        } else {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/word-icon.svg";
        }
    }

}
