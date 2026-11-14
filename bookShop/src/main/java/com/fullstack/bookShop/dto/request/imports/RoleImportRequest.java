package com.fullstack.bookShop.dto.request.imports;

import com.fullstack.bookShop.dto.request.RoleCreateRequest;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** Ke thua RoleCreateRequest de dung lai nguyen bo rang buoc validation, chi them cot Action. */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RoleImportRequest extends RoleCreateRequest {

    /** De trong thi hieu la CREATE - file theo template cu van dung duoc. */
    private ImportActionEnum action;
}
