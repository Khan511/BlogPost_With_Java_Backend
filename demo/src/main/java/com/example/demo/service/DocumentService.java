package com.example.demo.service;

// import java.util.Collection;
import java.util.List;
// import org.springframework.web.multipart.MultipartFile;
import com.example.demo.dto.DocumentDto;
import com.example.demo.entities.UserEntity;

public interface DocumentService {

    public List<DocumentDto> saveDocuments(UserEntity user, List<DocumentDto> documents);

    public void deleteDocument(Long documentId);

    public List<DocumentDto> getAllDocuments(Long id);

}
