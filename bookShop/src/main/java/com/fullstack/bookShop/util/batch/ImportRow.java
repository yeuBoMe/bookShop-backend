package com.fullstack.bookShop.util.batch;

import com.fullstack.bookShop.util.enums.ImportActionEnum;

/**
 * Mot dong da qua kiem tra, san sang ghi xuong DB.
 *
 * @param rowNumber so dong trong file, bat dau tu 1 - dung de bao loi dung cho
 * @param action    CREATE / UPDATE / DELETE
 * @param source    payload goc cua dong do
 */
public record ImportRow<S>(int rowNumber, ImportActionEnum action, S source) {
}
