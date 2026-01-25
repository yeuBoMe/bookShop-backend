package com.fullstack.bookShop.service.impl;

import com.cloudinary.Cloudinary;
import com.fullstack.bookShop.service.interfaces.CloudinaryService;
import com.fullstack.bookShop.util.cloudinary.CloudinaryUtil;
import com.fullstack.bookShop.util.constant.CloudinaryConstant;
import com.fullstack.bookShop.util.constant.SymbolConstant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    private final Cloudinary cloudinary;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> uploadImageToCloudinary(MultipartFile multipartFile) throws IOException {
        String fileName = System.currentTimeMillis() + SymbolConstant.UNDERSCORE + multipartFile.getOriginalFilename();
        LOGGER.info("Uploading file to Cloudinary with name={}, size={}", fileName, multipartFile.getSize());

        String fileNameRemoveDot = CloudinaryUtil.generatePublicId(fileName);
        LOGGER.debug("Generated Cloudinary publicId={}", fileNameRemoveDot);

        Map<String, Object> filePushToCloudinary = Map.of(
                CloudinaryConstant.PUBLIC_ID, fileNameRemoveDot,
                CloudinaryConstant.OVERWRITE, false
        );

        Map<String, Object> result = cloudinary.uploader().upload(multipartFile.getBytes(), filePushToCloudinary);
        LOGGER.info(
                "Upload file to Cloudinary successfully with publicId={}, url={}",
                fileNameRemoveDot,
                result.get(CloudinaryConstant.SECURE_URL)
        );

        return result;
    }

    @Override
    public List<Map<String, Object>> uploadMultipleImagesToCloudinary(
            List<MultipartFile> multipartFileList
    ) throws IOException {

        LOGGER.info("Uploading multiple files to Cloudinary with size={}", multipartFileList.size());
        List<Map<String, Object>> listFilesPushToCloudinary = new ArrayList<>();

        for (MultipartFile file : multipartFileList) {
            Map<String, Object> filePushOnCloudinary = uploadImageToCloudinary(file);
            listFilesPushToCloudinary.add(filePushOnCloudinary);
        }

        LOGGER.info(
                "Upload multiple files to Cloudinary successfully with success size={}",
                listFilesPushToCloudinary.size()
        );

        return listFilesPushToCloudinary;
    }

    @Override
    public void deleteImageFromCloudinary(String publicId) throws IOException {
        LOGGER.info("Deleting file from Cloudinary, publicId={}", publicId);

        cloudinary.uploader().destroy(publicId, Map.of());

        LOGGER.info("Delete file from Cloudinary successfully with publicId={}", publicId);
    }

    @Override
    public void deleteMultipleImagesFromCloudinary(List<String> publicIds) throws Exception {
        LOGGER.info("Deleting multiple files from Cloudinary, total={}", publicIds.size());

        cloudinary.api().deleteResources(publicIds, Map.of(CloudinaryConstant.INVALIDATE, true));

        LOGGER.info("Delete multiple files from Cloudinary successfully with total={}", publicIds.size());
    }

    @Override
    public String getFilePushedFromCloudinary(Map<String, Object> filePushToCloudinary) {
        return filePushToCloudinary.get(CloudinaryConstant.PUBLIC_ID) + SymbolConstant.DOT
                + filePushToCloudinary.get(CloudinaryConstant.FORMAT);
    }
}
