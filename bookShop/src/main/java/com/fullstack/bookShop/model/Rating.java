package com.fullstack.bookShop.model;

import com.fullstack.bookShop.model.base.AuditableEntity;
import com.fullstack.bookShop.util.constant.entity.RatingConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "ratings",
        indexes = {
                @Index(name = "idx_ratings_book_id_stars", columnList = "book_id, stars"),
                @Index(name = "idx_ratings_user_id", columnList = "user_id"),
                @Index(name = "idx_ratings_created_at_id", columnList = "created_at, id")
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rating extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ratings_id_seq")
    @SequenceGenerator(name = "ratings_id_seq", sequenceName = "ratings_id_seq", allocationSize = 50)
    private Long id;

    @Min(value = 1, message = RatingConstant.STARS_MIN)
    @Max(value = 5, message = RatingConstant.STARS_MAX)
    private int stars;

    @NotBlank(message = RatingConstant.COMMENT_REQUIRED)
    @Size(max = 200, message = RatingConstant.COMMENT_MAX_LENGTH)
    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
}
