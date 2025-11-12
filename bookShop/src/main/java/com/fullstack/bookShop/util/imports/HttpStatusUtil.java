package com.fullstack.bookShop.util.imports;

import com.fullstack.bookShop.dto.imports.RestImportDTO;
import org.springframework.http.HttpStatus;

public class HttpStatusUtil {

    public static <T> HttpStatus determineHttpStatus(RestImportDTO<T> restImportDTO) {
        if (restImportDTO.getCountSuccess() == 0) {
            return HttpStatus.BAD_REQUEST;
        } else if (restImportDTO.getCountError() > 0) {
            return HttpStatus.MULTI_STATUS;
        } else {
            return HttpStatus.CREATED;
        }
    }
}
