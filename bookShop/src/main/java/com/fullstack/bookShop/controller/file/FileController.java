package com.fullstack.bookShop.controller.file;

import com.fullstack.bookShop.domain.dto.file.RestUploadFileDTO;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.exception.custom.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @ApiMessage("Upload single file")
    @PostMapping
    public ResponseEntity<RestUploadFileDTO> uploadSingleFileRequest(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "folder", required = false) String folder
    ) throws
            URISyntaxException,
            IOException,
            StorageException {
        // validate file
        if (file.isEmpty() || file.getSize() == 0) {
            throw new StorageException("File is empty. Please upload a file");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "docx", "pdf");

        boolean isValid = allowedExtensions.stream()
                .anyMatch(extension -> {
                    if (fileName != null) {
                        return fileName.toLowerCase().endsWith(extension);
                    } else {
                        return false;
                    }
                });

        if (!isValid) {
            throw new StorageException("Invalid file extension. Please upload a file.");
        }

        // create dir if not exist
        this.fileService.createUploadFolder(this.baseURI + folder);

        // store file
        String fileUploaded = this.fileService.storeFile(file, folder);
        RestUploadFileDTO restUploadFileDTO = new RestUploadFileDTO(fileUploaded, Instant.now());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(restUploadFileDTO);
    }
}
