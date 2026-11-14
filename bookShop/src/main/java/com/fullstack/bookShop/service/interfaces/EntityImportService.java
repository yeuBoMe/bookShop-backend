package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;

import java.util.List;

/**
 * Import cho phep moi dong mang thao tac rieng (CREATE / UPDATE / DELETE)
 * va bao cao ket qua theo TUNG DONG.
 *
 * @param <S> kieu request cua mot dong
 * @param <R> kieu response tra ve cho dong thanh cong
 */
public interface EntityImportService<S, R> {

    ImportFileResponse<R> importFromFile(List<S> requests);
}
