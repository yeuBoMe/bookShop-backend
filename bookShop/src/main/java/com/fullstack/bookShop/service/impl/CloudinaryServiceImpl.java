package com.fullstack.bookShop.service.impl;

import com.cloudinary.Cloudinary;
import com.fullstack.bookShop.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImageToCloudinary(MultipartFile multipartFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

        String fileNameRemoveDotPart = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf("."))
                : fileName;

        Map<String, Object> fileNameInCloudinary = Map.of(
                "public_id", fileNameRemoveDotPart,
                "overwrite", false
        );

        return cloudinary.uploader().upload(multipartFile.getBytes(), fileNameInCloudinary);
    }

    @Override
    public List<Map<String, Object>> uploadListImagesToCloudinary(
            List<MultipartFile> multipartFileList
    ) throws IOException {
        List<Map<String, Object>> imagesUploadedToCloudinary = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFileList) {
            Map<String, Object> imageUploadedToCloudinary = uploadImageToCloudinary(multipartFile);
            imagesUploadedToCloudinary.add(imageUploadedToCloudinary);
        }
        return imagesUploadedToCloudinary;
    }

    @Override
    public void deleteImageFromCloudinary(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, Map.of());
    }

    @Override
    public void deleteMultipleImagesFromCloudinary(List<String> publicIds) throws Exception {
        cloudinary.api().deleteResources(publicIds, Map.of("invalidate", true));
    }
}
