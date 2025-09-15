package com.fullstack.bookShop.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

public interface FileService {

    void createUploadFolder(String folder) throws URISyntaxException;

    String storeFile(MultipartFile file, String folder) throws URISyntaxException, IOException;
}
