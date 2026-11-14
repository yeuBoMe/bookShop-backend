package com.fullstack.bookShop.service.impl.imports;

import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.service.interfaces.EntityImportService;
import com.fullstack.bookShop.util.batch.ImportExecutor;
import com.fullstack.bookShop.util.batch.ImportOutcome;
import com.fullstack.bookShop.util.batch.ImportResponseUtil;
import com.fullstack.bookShop.util.batch.ImportRow;
import com.fullstack.bookShop.util.constant.MessageKeyConstant;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Khung chung cho moi luong import theo dong.
 *
 * <p>Ba nhip:
 * <ol>
 *   <li><b>Kiem tra</b> - thuan bo nho, khong cham DB. Dong hong bi loai ngay nen khong ton
 *       mot lan mo transaction.</li>
 *   <li><b>Nap san</b> - lay TAT CA du lieu tra cuu bang mot vai cau query. Neu de moi dong tu tra
 *       DB thi 5.000 dong sinh 15.000 cau thay vi 5.000.</li>
 *   <li><b>Ghi</b> - giao cho {@link ImportExecutor}: gom lo cho nhanh, lo hong thi chay lai tung dong.</li>
 * </ol>
 *
 * <p>Tach kiem tra khoi ghi con de viec chay lai tung dong an toan: moi trang thai kiem tra
 * (vi du chong trung trong cung file) da chot xong o nhip mot, khong bi lech khi nhip ba chay lai.
 *
 * @param <C> du lieu tra cuu nap san dung chung cho ca file
 */
@RequiredArgsConstructor
public abstract class AbstractEntityImportService<S, E, R, C> implements EntityImportService<S, R> {

    protected final Validator validator;

    protected final ImportExecutor importExecutor;

    protected final MessageTranslator messageTranslator;

    @Override
    public ImportFileResponse<R> importFromFile(List<S> requests) {
        List<ImportOutcome<S, R>> rejectedRows = new ArrayList<>();
        List<ImportRow<S>> acceptedRows = new ArrayList<>();
        Map<String, Integer> firstRowNumberByKey = new HashMap<>();

        for (int index = 0; index < requests.size(); index++) {
            int rowNumber = index + 1;
            S request = requests.get(index);
            ImportActionEnum action = ImportActionEnum.resolveOrCreate(actionOf(request));

            Object error = validateRow(request, action, rowNumber, firstRowNumberByKey);
            if (error != null) {
                rejectedRows.add(ImportOutcome.failure(rowNumber, request, error));
                continue;
            }

            acceptedRows.add(new ImportRow<>(rowNumber, action, request));
        }

        C context = buildContext(acceptedRows);

        List<ImportOutcome<S, R>> executedRows =
                importExecutor.execute(acceptedRows, row -> handleRow(row, context));

        List<ImportOutcome<S, R>> allRows = Stream.concat(rejectedRows.stream(), executedRows.stream())
                .sorted(Comparator.comparingInt(ImportOutcome::rowNumber))
                .toList();

        long successCount = allRows.stream().filter(ImportOutcome::isSuccess).count();
        long errorCount = allRows.size() - successCount;

        return ImportResponseUtil.buildResponse(
                allRows,
                messageTranslator.toLocale(MessageKeyConstant.IMPORT_SUCCESS, successCount, errorCount)
        );
    }

    private Object validateRow(
            S request,
            ImportActionEnum action,
            int rowNumber,
            Map<String, Integer> firstRowNumberByKey
    ) {
        String businessKey = StringUtils.trimLowerBlank(businessKeyOf(request));
        if (businessKey == null) {
            return messageTranslator.toLocale(MessageKeyConstant.IMPORT_KEY_REQUIRED, action);
        }

        Integer duplicatedRowNumber = firstRowNumberByKey.putIfAbsent(businessKey, rowNumber);
        if (duplicatedRowNumber != null) {
            return messageTranslator.toLocale(MessageKeyConstant.IMPORT_DUPLICATED_ROW, duplicatedRowNumber);
        }

        // DELETE chi can khoa dinh danh, khong the bat khai day du cac cot con lai
        if (action == ImportActionEnum.DELETE) {
            return null;
        }

        List<String> violations = validator.validate(request)
                .stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();

        return violations.isEmpty() ? null : violations;
    }

    private R handleRow(ImportRow<S> row, C context) {
        S request = row.source();

        return switch (row.action()) {
            case CREATE -> toResponse(createEntity(request, context), ImportActionEnum.CREATE);
            case UPDATE -> toResponse(
                    updateEntity(findRequiredEntity(request), request, context),
                    ImportActionEnum.UPDATE);
            case DELETE -> {
                E existing = findRequiredEntity(request);
                R response = toResponse(existing, ImportActionEnum.DELETE);
                deleteEntity(existing);
                yield response;
            }
        };
    }

    private E findRequiredEntity(S request) {
        return findExistingEntity(request)
                .orElseThrow(() -> new IllegalArgumentException(notFoundMessage(request)));
    }

    /** Nap truoc du lieu tra cuu dung chung cho ca file, chay DUNG mot lan. */
    protected abstract C buildContext(List<ImportRow<S>> rows);

    /** Khoa nghiep vu cua dong, dung de chong trung trong cung file (vi du name + author). */
    protected abstract String businessKeyOf(S request);

    protected abstract ImportActionEnum actionOf(S request);

    /** Chi duoc goi cho UPDATE / DELETE - CREATE tra cuu qua context de khong ton them query. */
    protected abstract Optional<E> findExistingEntity(S request);

    protected abstract String notFoundMessage(S request);

    protected abstract E createEntity(S request, C context);

    protected abstract E updateEntity(E existing, S request, C context);

    protected abstract void deleteEntity(E existing);

    protected abstract R toResponse(E entity, ImportActionEnum action);
}
