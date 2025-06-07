package com.example.demo.service.serviceImplemnetaion;

// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
// import java.util.Collection;
// import java.util.Collection;
import java.util.List;
// import java.util.Objects;
import java.util.UUID;
// import java.util.stream.Collector;

import static org.apache.commons.io.FilenameUtils.getExtension;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;
// import org.springframework.web.multipart.MultipartFile;
// import com.example.demo.constant.Constant;
import com.example.demo.dto.DocumentDto;
import com.example.demo.entities.DocumentEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.repo.DocumentRepo;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import com.example.demo.utils.DocumentUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final UserService userService;
    private final DocumentRepo documentRepo;

    @Override
    public List<DocumentDto> saveDocuments(UserEntity user, List<DocumentDto> documents) throws FileUploadException {
        List<DocumentDto> newDocuments = new ArrayList<>();
        try {
            for (DocumentDto document : documents) {
                DocumentEntity documentEntity = DocumentEntity.builder()
                        .createdBy(user.getId())
                        .updatedBy(user.getId())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .documentId(UUID.randomUUID().toString())
                        .name(document.getName())
                        .size(document.getSize())
                        .uri(document.getUri())
                        .extension(getExtension(document.getName()))
                        .icon(DocumentUtils.setIcon(getExtension(document.getName())))
                        .owner(user)
                        .formattedSize(FileUtils.byteCountToDisplaySize(document.getSize()))
                        .build();

                DocumentEntity saveDocument = documentRepo.save(documentEntity);
                DocumentDto documentDto = DocumentUtils.toDocumentDto(user, saveDocument);
                newDocuments.add(documentDto);
            }
            return newDocuments;
        } catch (Exception e) {
            throw new FileUploadException("Failed to save document(s).", e);
            // throw new RuntimeException("Failed to save document(s).", e);

        }
    }

    @Override
    public List<DocumentDto> getAllDocuments(Long id) {
        UserEntity user = userService.getUserById(id);
        List<DocumentEntity> documents = documentRepo.findAllByCreatedBy(id);
        return documents.stream().map(doc -> DocumentUtils.toDocumentDto(user, doc)).toList();
    }

    @Override
    public void deleteDocument(Long documentId) {
        documentRepo.deleteById(documentId);
    }

    // // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // // SAVING THE DOCUMENTS IN COMPUTERS
    // @Override
    // public Collection<DocumentDto> saveDocuments(String userId,
    // List<MultipartFile> documents) {

    // List<DocumentDto> newDocuments = new ArrayList<>();
    // UserEntity user = userService.getByUserId(userId);
    // Path storage = Paths.get(Constant.FILE_STORAGE).toAbsolutePath().normalize();

    // try {
    // for (MultipartFile document : documents) {

    // String fileName =
    // StringUtils.cleanPath(Objects.requireNonNull(document.getOriginalFilename()));

    // String extension = getExtension(fileName);
    // String newFileName = fileName + UUID.randomUUID().toString() + "." +
    // extension;

    // if ("..".contains(newFileName)) {
    // throw new RuntimeException(String.format("Invalid file name %s",
    // newFileName));
    // }

    // DocumentEntity documentEntity = DocumentEntity.builder()
    // .documentId(UUID.randomUUID().toString())
    // .name(newFileName)
    // .owner(user)
    // .extension(getExtension(newFileName))
    // .uri(DocumentUtils.getDocumentUri(newFileName))
    // .formattedSize(FileUtils.byteCountToDisplaySize(document.getSize()))
    // .size(document.getSize())
    // .icon(DocumentUtils.setIcon(getExtension(newFileName)))
    // .build();

    // DocumentEntity saveDocument = documentRepo.save(documentEntity);

    // Path targetLocation = storage.resolve(newFileName);

    // Files.copy(document.getInputStream(), targetLocation);

    // DocumentDto newDocument = DocumentUtils
    // .toDocumentDto(userService.getByUserId(saveDocument.getOwner().getUserId()),
    // documentEntity);

    // newDocuments.add(newDocument);
    // }
    // return newDocuments;

    // } catch (Exception e) {
    // log.error("Failed to upload files=================================", e);
    // throw new RuntimeException("Failed to upload files", e);
    // }
    //
    // }

}
