package com.fullstack.bookShop.util.batch;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Chay import theo tung dong doc lap: mot dong hong khong lam hong dong khac.
 *
 * <p><b>Vi sao khong bao ca file trong mot @Transactional:</b> lam vay thi mot dong hong o luc flush
 * la cuon nguoc toan bo file, va file vai nghin dong giu lock rat lau.
 *
 * <p><b>Vi sao van con transaction:</b> mo mot transaction cho MOI dong thi 5.000 dong la 5.000 lan
 * begin/commit. Nen di duong hai nhip: gom thanh lo cho nhanh, lo nao hong thi chay lai TUNG DONG
 * trong dung lo do de biet chinh xac dong nao hong va van luu duoc cac dong lanh cung lo.
 *
 * <p>Danh doi CO Y: khong con bao dam nguyen tu tren toan file. Do dung la thu nghiep vu import can -
 * bao cao "bao nhieu dong duoc, bao nhieu dong hong" thay vi duoc tat ca hoac khong duoc gi.
 */
@Component
@RequiredArgsConstructor
public class ImportExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExecutor.class);

    private final PlatformTransactionManager transactionManager;

    @Value("${bookshop.import.chunk-size}")
    private int chunkSize;

    /**
     * @param rows       cac dong da xac dinh duoc thao tac, theo dung thu tu trong file
     * @param rowHandler ghi xuong DB cho mot dong, nem exception neu dong do hong
     */
    public <S, R> List<ImportOutcome<S, R>> execute(
            List<ImportRow<S>> rows,
            Function<ImportRow<S>, R> rowHandler
    ) {
        List<ImportOutcome<S, R>> outcomes = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return outcomes;
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        int effectiveChunkSize = Math.max(1, chunkSize);

        for (int start = 0; start < rows.size(); start += effectiveChunkSize) {
            int end = Math.min(start + effectiveChunkSize, rows.size());
            List<ImportRow<S>> chunk = List.copyOf(rows.subList(start, end));

            List<ImportOutcome<S, R>> chunkOutcomes = executeChunk(transactionTemplate, chunk, rowHandler);
            outcomes.addAll(chunkOutcomes);
        }

        return outcomes;
    }

    private <S, R> List<ImportOutcome<S, R>> executeChunk(
            TransactionTemplate transactionTemplate,
            List<ImportRow<S>> chunk,
            Function<ImportRow<S>, R> rowHandler
    ) {
        try {
            return transactionTemplate.execute(status -> {
                List<ImportOutcome<S, R>> results = new ArrayList<>();
                for (ImportRow<S> row : chunk) {
                    results.add(ImportOutcome.success(row.rowNumber(), row.source(), rowHandler.apply(row)));
                }
                return results;
            });

        } catch (RuntimeException ex) {
            // Ca lo bi cuon nguoc. Chay lai tung dong de dong lanh van vao duoc va dong hong duoc goi ten.
            LOGGER.warn("Import chunk rolled back ({} rows), retrying row by row: {}",
                    chunk.size(), ex.getMessage());
            return retryRowByRow(transactionTemplate, chunk, rowHandler);
        }
    }

    private <S, R> List<ImportOutcome<S, R>> retryRowByRow(
            TransactionTemplate transactionTemplate,
            List<ImportRow<S>> chunk,
            Function<ImportRow<S>, R> rowHandler
    ) {
        List<ImportOutcome<S, R>> outcomes = new ArrayList<>();

        for (ImportRow<S> row : chunk) {
            try {
                R result = transactionTemplate.execute(status -> rowHandler.apply(row));
                outcomes.add(ImportOutcome.success(row.rowNumber(), row.source(), result));

            } catch (RuntimeException ex) {
                String reason = rootCauseMessage(ex);
                LOGGER.warn("Import row {} failed: {}", row.rowNumber(), reason);
                outcomes.add(ImportOutcome.failure(row.rowNumber(), row.source(), reason));
            }
        }

        return outcomes;
    }

    /** Lay thong diep sat goc nhat, thay vi lop wrapper cua Spring von khong noi len dieu gi voi nguoi dung. */
    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }
}
