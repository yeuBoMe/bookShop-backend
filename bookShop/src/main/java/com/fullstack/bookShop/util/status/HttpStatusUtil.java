package com.fullstack.bookShop.util.status;

import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import org.springframework.http.HttpStatus;

public final class HttpStatusUtil {

    private HttpStatusUtil() {}

    public static <T> HttpStatus determineHttpStatus(ImportFileResponse<T> importFileResponse) {
        if (importFileResponse.getCountSuccess() == 0) {
            return HttpStatus.BAD_REQUEST;
        } else if (importFileResponse.getCountError() > 0) {
            return HttpStatus.MULTI_STATUS;
        } else {
            return HttpStatus.CREATED;
        }
    }
}
