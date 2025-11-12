package com.fullstack.bookShop.controller.file;

import com.fullstack.bookShop.dto.file.RestUploadFileDTO;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.exception.custom.StorageException;
import com.fullstack.bookShop.util.imports.FileValidUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;

    @ApiMessage("Upload single file")
    @PostMapping("/single")
    public ResponseEntity<RestUploadFileDTO> uploadSingleFileRequest(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "folder", required = false) String folder
    ) throws
            URISyntaxException,
            IOException,
            StorageException {
        // validate file
        FileValidUtil.validateFile(file);

        // create dir if not exist
        this.fileService.createUploadFolder(this.baseURI + folder);

        // store file
        String fileUploaded = this.fileService.storeSingleFile(file, folder);
        RestUploadFileDTO restUploadFileDTO = new RestUploadFileDTO(fileUploaded, Instant.now());

        return ResponseEntity.ok(restUploadFileDTO);
    }

    @ApiMessage("Upload multiple files")
    @PostMapping("/multiple")
    public ResponseEntity<RestUploadFileDTO> uploadMultipleFileRequest(
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @RequestParam(name = "folder", required = false) String folder
    ) throws
            URISyntaxException,
            IOException,
            StorageException {
        // validate files
        FileValidUtil.validateListFiles(files);

        // create dir if not exist
        this.fileService.createUploadFolder(this.baseURI + folder);

        // store file
        List<String> fileListUploaded = this.fileService.storeMultipleFiles(files, folder);
        RestUploadFileDTO restUploadFileDTO = new RestUploadFileDTO(fileListUploaded, Instant.now());

        return ResponseEntity.ok(restUploadFileDTO);
    }
}
