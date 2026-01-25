package com.fullstack.bookShop.util.cloudinary;

import com.fullstack.bookShop.util.constant.SymbolConstant;

import java.util.List;

public final class CloudinaryUtil {

    private CloudinaryUtil() {}

    public static String generatePublicId(String fileName) {
        return fileName.contains(SymbolConstant.DOT)
                ? fileName.substring(0, fileName.lastIndexOf(SymbolConstant.DOT))
                : fileName;
    }

    public static List<String> generateListPublicIds(List<String> fileNames) {
        return fileNames.stream()
                .map(CloudinaryUtil::generatePublicId)
                .toList();
    }
}
