package com.fullstack.bookShop.util.batch;

/**
 * Ket qua cua DUNG mot dong import. Mot dong hong khong keo theo dong nao khac.
 *
 * @param rowNumber so dong trong file, bat dau tu 1
 * @param source    payload goc, tra lai cho FE khi dong nay hong
 * @param result    ket qua map ra response khi dong nay thanh cong
 * @param error     null neu thanh cong, nguoc lai la thong diep hoac danh sach thong diep loi
 */
public record ImportOutcome<S, R>(int rowNumber, S source, R result, Object error) {

    public static <S, R> ImportOutcome<S, R> success(int rowNumber, S source, R result) {
        return new ImportOutcome<>(rowNumber, source, result, null);
    }

    public static <S, R> ImportOutcome<S, R> failure(int rowNumber, S source, Object error) {
        return new ImportOutcome<>(rowNumber, source, null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
