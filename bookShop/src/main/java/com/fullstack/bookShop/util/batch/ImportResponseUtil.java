package com.fullstack.bookShop.util.batch;

import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;

import java.util.ArrayList;
import java.util.List;

/** Gom ket qua tung dong thanh dung khuon ImportFileResponse ma man hinh import dang doc. */
public final class ImportResponseUtil {

    private ImportResponseUtil() {}

    public static <S, R> ImportFileResponse<R> buildResponse(
            List<ImportOutcome<S, R>> outcomes,
            String successMessage
    ) {
        List<R> successItems = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<S>> faults = new ArrayList<>();

        for (ImportOutcome<S, R> outcome : outcomes) {
            if (outcome.isSuccess()) {
                successItems.add(outcome.result());
            } else {
                faults.add(new ImportFileResponse.DetailFaultInfo<>(
                        outcome.rowNumber(),
                        outcome.error(),
                        outcome.source()
                ));
            }
        }

        ImportFileResponse<R> response = new ImportFileResponse<>();
        response.setCountSuccess(successItems.size());
        response.setCountError(faults.size());
        response.setDetail(faults.isEmpty() ? successMessage : faults);
        response.setItems(successItems);
        return response;
    }
}
