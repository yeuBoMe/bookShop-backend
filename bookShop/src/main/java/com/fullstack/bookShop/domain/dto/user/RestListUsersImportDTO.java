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
public class RestListUsersImportDTO {
    private int countSuccess;
    private int countError;
    private List<RestUserCreateDTO> users;
}
