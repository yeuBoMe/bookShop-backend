package com.fullstack.bookShop.controller.cloudinary;

import com.fullstack.bookShop.service.interfaces.CloudinaryService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    @ApiMessage("Upload image to cloudinary")
    public ResponseEntity<Map<String, Object>> uploadImageToCloudinaryRequest(
            @RequestParam("file") MultipartFile multipartFile
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cloudinaryService.uploadImageToCloudinary(multipartFile));
    }
}
