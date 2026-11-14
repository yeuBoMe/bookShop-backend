package com.fullstack.bookShop.util.export;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ghi CSV thang ra luong dau ra cua response, khong dung buffer toan bo trong bo nho.
 *
 * <p>Co dau BOM o dau file: thieu no thi Excel doc file UTF-8 theo bang ma he thong
 * va tieng Viet ra chu vuong.
 */
public final class CsvStreamWriter implements Closeable {

    private static final char QUOTE = '"';

    private static final String LINE_BREAK = "\r\n";

    /** BOM UTF-8. Excel dua vao day de nhan ra file la UTF-8. */
    private static final String BYTE_ORDER_MARK = "﻿";

    /**
     * Ky tu mo dau khien Excel/Sheets coi o do la CONG THUC.
     * Ten sach do nguoi dung nhap ma bat dau bang '=' thi mo file len la cong thuc chay that
     * (CSV injection). Chen mot dau nhay don o truoc de Excel hieu day la van ban.
     */
    private static final String FORMULA_TRIGGERS = "=+-@\t\r";

    private final Writer writer;

    private final String separator;

    public CsvStreamWriter(OutputStream outputStream, String separator) throws IOException {
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.separator = separator;
        this.writer.write(BYTE_ORDER_MARK);
    }

    public void writeRow(List<String> cells) throws IOException {
        StringBuilder line = new StringBuilder();

        for (int index = 0; index < cells.size(); index++) {
            if (index > 0) {
                line.append(separator);
            }
            line.append(escape(neutralizeFormula(cells.get(index))));
        }

        line.append(LINE_BREAK);
        writer.write(line.toString());
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
    }

    private String neutralizeFormula(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return FORMULA_TRIGGERS.indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    }

    /**
     * O chua dau phan cach, dau nhay hoac xuong dong thi phai boc trong nhay kep,
     * va nhay kep ben trong phai nhan doi. Khong lam vay la vo cau truc file.
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuoting = value.contains(separator)
                || value.indexOf(QUOTE) >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;

        if (!needsQuoting) {
            return value;
        }

        return QUOTE + value.replace(String.valueOf(QUOTE), "\"\"") + QUOTE;
    }
}
