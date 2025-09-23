package com.fullstack.bookShop.util.imports;

import com.fullstack.bookShop.util.exception.custom.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidUtil {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "docx", "pdf");

    public static void validateFile(MultipartFile multipartFile) throws StorageException {
        if (multipartFile.isEmpty() || multipartFile.getSize() == 0) {
            throw new StorageException("File is empty. Please upload a file");
        }

        String fileName = multipartFile.getOriginalFilename();

        boolean isValid = ALLOWED_EXTENSIONS.stream()
                .anyMatch(extension -> {
                    if (fileName != null) {
                        return fileName.toLowerCase().endsWith(extension);
                    } else return false;
                });

        if (!isValid) {
            throw new StorageException("Invalid file extension. Please upload a file.");
        }
    }

    public static void validateListFiles(List<MultipartFile> files) throws StorageException {
        for (MultipartFile file : files) {
            validateFile(file);
        }
    }
}
