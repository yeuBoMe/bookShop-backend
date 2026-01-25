package com.fullstack.bookShop.util.pagination;

import com.fullstack.bookShop.dto.response.pagination.Meta;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public final class PaginationUtil {

    private PaginationUtil() {}

    public static <T> PaginationResponse fetchAllDataWithPagination(Page<T> pageHavData, Pageable pageable) {
        Meta meta = new Meta();
        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageHavData.getTotalPages());
        meta.setTotal(pageHavData.getTotalElements());

        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setMeta(meta);
        return paginationResponse;
    }
}
