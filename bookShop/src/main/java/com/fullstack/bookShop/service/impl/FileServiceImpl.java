package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.service.interfaces.FileService;
import com.fullstack.bookShop.util.constant.SymbolConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${bookshop.upload-file.base-uri}")
    private String baseURI;

    @Override
    public void createUploadFolder(String folder) throws URISyntaxException, IOException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);

        File tempDir = new File(path.toString());
        if (!tempDir.isDirectory()) {
            try {
                Files.createDirectories(tempDir.toPath());
                LOGGER.info("Create directory successfully, path={}", tempDir.toPath());
            } catch (IOException ex) {
                LOGGER.error("Upload folder failed: {}", ex.getMessage(), ex);
                throw ex;
            }
        } else {
            LOGGER.info("Skipping create directory, already exists");
        }
    }

    @Override
    public String storeSingleFile(MultipartFile file, String folder) throws URISyntaxException, IOException {
        String fileName = System.currentTimeMillis() + SymbolConstant.UNDERSCORE + file.getOriginalFilename();
        URI uri = new URI(baseURI + folder + SymbolConstant.FORWARD_SLASH + fileName);
        Path path = Paths.get(uri);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Store file successfully with name: {}", fileName);
        }
        return fileName;
    }

    @Override
    public List<String> storeMultipleFiles(
            List<MultipartFile> files, String folder
    ) throws URISyntaxException, IOException {

        List<String> fileList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = storeSingleFile(file, folder);
            fileList.add(fileName);
        }
        return fileList;
    }

    @Override
    public void deleteSingleFile(String fileName, String folder) throws URISyntaxException, IOException {
        URI uri = new URI(baseURI + folder + SymbolConstant.FORWARD_SLASH + fileName);
        Path path = Paths.get(uri);

        if (Files.exists(path)) {
            Files.delete(path);
            LOGGER.info("Delete file successfully with name: {}", fileName);
        } else {
            LOGGER.info("File not found, skipping delete with path: {}", path);
        }
    }

    @Override
    public void deleteMultipleFiles(List<String> fileNames, String folder) throws URISyntaxException, IOException {
        for (String fileName : fileNames) {
            deleteSingleFile(fileName, folder);
        }
    }
}
