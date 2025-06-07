package com.example.demo.controller;

import java.net.URI;
import java.util.Map;
import java.util.List;
// import java.util.Collection;
import java.util.Collections;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static com.example.demo.utils.RequestUtils.getResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;
import com.example.demo.domain.Response;
import com.example.demo.dto.DocumentDto;
// import com.example.demo.entities.DocumentEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
public class DocumentController {
        private final UserService userService;
        private final DocumentService documentService;

        @PostMapping("/upload-documents")
        public ResponseEntity<Response> uploadDocuments(@AuthenticationPrincipal UserDetails userDetails,
                        @RequestBody List<DocumentDto> documents, HttpServletRequest request)
                        throws FileUploadException {

                UserEntity user = userService.getUserByName(userDetails.getUsername());

                List<DocumentDto> newDocuments = documentService.saveDocuments(user, documents);

                return ResponseEntity.created(URI.create(""))
                                .body(getResponse(request,
                                                Map.of("documents",
                                                                newDocuments != null ? newDocuments
                                                                                : Collections.emptyList()),
                                                "Document(s) uploaded successfully", HttpStatus.CREATED));
        }

        @DeleteMapping("/document-delete/{documentId}")
        public ResponseEntity<Response> deleteDocument(@PathVariable Long documentId, HttpServletRequest request) {

                documentService.deleteDocument(documentId);

                return ResponseEntity.ok()
                                .body(getResponse(request, Map.of(), "Document Deleted successfully.", HttpStatus.OK));
        }

        @GetMapping("/documents")
        public ResponseEntity<Response> getAllDocuments(@AuthenticationPrincipal UserDetails userDetails,
                        HttpServletRequest request) {

                UserEntity user = userService.getUserByName(userDetails.getUsername());
                List<DocumentDto> allDocuments = documentService.getAllDocuments(user.getId());

                return ResponseEntity.ok()
                                .body(getResponse(request, Map.of("documents", allDocuments),
                                                "Document Deleted successfully.",
                                                HttpStatus.OK));
        }

        // // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // // SAVING THE DOCUMENTS IN COMPUTERS
        // @PostMapping("/upload-documents")
        // public ResponseEntity<Response> uploadDocuments(@AuthenticationPrincipal
        // UserDetails userDetails,
        // @RequestParam("files") List<MultipartFile> documents, HttpServletRequest
        // request) {
        // try {

        // UserEntity user = userService.getUserByName(userDetails.getUsername());

        // Collection<DocumentDto> newDocuments =
        // documentService.saveDocuments(user.getUserId(), documents);

        // return ResponseEntity.created(URI.create(""))
        // .body(getResponse(request,
        // Map.of("documents", newDocuments != null ? newDocuments :
        // Collections.emptyList()),
        // "Document(s) uploaded successfully", HttpStatus.CREATED));

        // } catch (Exception e) {
        // System.out.println("Error uplaod files" + e);
        // throw new RuntimeException("Error upload file", e);
        // }
        // }

}
