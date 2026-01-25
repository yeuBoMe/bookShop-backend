package com.fullstack.bookShop.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface FileService {

    void createUploadFolder(String folder) throws URISyntaxException;

    String storeSingleFile(MultipartFile file, String folder) throws URISyntaxException, IOException;

    List<String> storeMultipleFiles(List<MultipartFile> files, String folder) throws URISyntaxException, IOException;

    void deleteSingleFile(String fileName, String folder) throws URISyntaxException, IOException;

    void deleteMultipleFiles(List<String> fileNames, String folder) throws URISyntaxException, IOException;
}
