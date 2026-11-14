package com.fullstack.bookShop.util.enums;

/** Thao tac cua mot dong trong file import. Cot Action de trong thi hieu la CREATE. */
public enum ImportActionEnum {
    CREATE,
    UPDATE,
    DELETE;

    public static ImportActionEnum resolveOrCreate(ImportActionEnum action) {
        return action != null ? action : CREATE;
    }
}
