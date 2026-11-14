package com.fullstack.bookShop.util.file;

import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class FileValidUtil {

    private FileValidUtil() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidUtil.class);

    private static final List<String> ALLOWED_EXTENSIONS
            = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "docx", "pdf");

    public static void validateSingleFile(MultipartFile multipartFile) throws FileException {
        if (multipartFile.isEmpty() || multipartFile.getSize() == 0) {
            LOGGER.warn("File validation failed: empty file (size={})", multipartFile.getSize());
            throw new FileException(CommonMessage.FILE_EMPTY);
        }

        String fileName = multipartFile.getOriginalFilename();
        LOGGER.info("Validating file: name={}, size={} bytes", fileName, multipartFile.getSize());

        boolean isValid = ALLOWED_EXTENSIONS.stream()
                .anyMatch(extension -> StringUtils.convertValueTrimLower(Objects.requireNonNull(fileName))
                        .endsWith(extension)
                );

        if (!isValid) {
            LOGGER.warn("File validation failed: file {} has invalid extension", fileName);
            throw new FileException(CommonMessage.FILE_INVALID);
        }
    }

    public static void validateMultipleFiles(List<MultipartFile> multipartFiles) throws FileException {
        for (MultipartFile multipartFile : multipartFiles) {
            validateSingleFile(multipartFile);
        }
    }
}
