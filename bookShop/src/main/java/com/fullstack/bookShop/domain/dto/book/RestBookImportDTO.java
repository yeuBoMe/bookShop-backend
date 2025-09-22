package com.fullstack.bookShop.domain.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestBookImportDTO {
    private int countSuccess;
    private int countError;
    private Object detail;
    private List<RestBookCreateDTO> books;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailFaultInfo {
        private int index;
        private Object errorMessage;
        private RestBookCreateDTO book;
    }
}
