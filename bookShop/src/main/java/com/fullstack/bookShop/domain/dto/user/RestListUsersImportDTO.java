package com.fullstack.bookShop.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestListUsersImportDTO<T> {
    private int countSuccess;
    private int countError;
    private T detail;
    private List<RestUserCreateDTO> users;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailFaultInfo<E> {
        private int index;
        private E errorMessage;
        private RestUserCreateDTO user;
    }
}
