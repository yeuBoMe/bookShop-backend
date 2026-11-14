package com.fullstack.bookShop.dto.request.imports;

import com.fullstack.bookShop.dto.request.user.UserCreateRequest;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** Ke thua UserCreateRequest de dung lai nguyen bo rang buoc validation, chi them cot Action. */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserImportRequest extends UserCreateRequest {

    /** De trong thi hieu la CREATE - file theo template cu van dung duoc. */
    private ImportActionEnum action;
}
