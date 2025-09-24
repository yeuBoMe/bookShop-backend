package com.fullstack.bookShop.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestImportDTO<T> {
    private int countSuccess;
    private int countError;
    private Object detail;
    private List<T> items;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailFaultInfo<U> {
        private int index;
        private Object errorMessage;
        private U item;
    }
}
