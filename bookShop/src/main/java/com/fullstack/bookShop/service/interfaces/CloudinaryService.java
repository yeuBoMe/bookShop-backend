package com.fullstack.bookShop.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CloudinaryService {

    Map<String, Object> uploadImageToCloudinary(MultipartFile multipartFile) throws IOException;

    List<Map<String, Object>> uploadMultipleImagesToCloudinary(List<MultipartFile> multipartFileList)
            throws IOException;

    void deleteImageFromCloudinary(String publicId) throws IOException;

    void deleteMultipleImagesFromCloudinary(List<String> publicIds) throws Exception;

    String getFilePushedFromCloudinary(Map<String, Object> filePushToCloudinary);
}
