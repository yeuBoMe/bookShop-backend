package com.fullstack.bookShop.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestUploadFileDTO {
    private Object fileName;
    private Instant uploadedAt;
}
