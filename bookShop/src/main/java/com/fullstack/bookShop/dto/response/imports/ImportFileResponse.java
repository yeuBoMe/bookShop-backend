package com.fullstack.bookShop.dto.response.imports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportFileResponse<T> {
    private int countSuccess;
    private int countError;
    private Object detail;
    private List<T> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailFaultInfo<U> {
        private int index;
        private Object errorMessage;
        private U item;
    }
}
