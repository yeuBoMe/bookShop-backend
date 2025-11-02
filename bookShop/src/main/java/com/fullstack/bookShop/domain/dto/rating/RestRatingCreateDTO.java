package com.fullstack.bookShop.domain.dto.rating;

import com.fullstack.bookShop.domain.entity.Book;
import com.fullstack.bookShop.domain.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestRatingCreateDTO {
    private Long id;
    private int stars;
    private String comment;
    private String user;
    private String book;
    private Instant createdAt;
    private String createdBy;
}
