package com.fullstack.bookShop.service.impl;

import com.cloudinary.Cloudinary;
import com.fullstack.bookShop.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImageToCloudinary(MultipartFile multipartFile) throws IOException {
        String originalFileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

        String fileNameRemoveDot = originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf("."))
                : originalFileName;

        Map<String, Object> fileNameInCloudinary = Map.of(
                "public_id", fileNameRemoveDot,
                "overwrite", false
        );

        return cloudinary.uploader().upload(multipartFile.getBytes(), fileNameInCloudinary);
    }
}
