package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Ghi lai diem trung binh + so luot danh gia bang MOT cau lenh, khong nap entity.
     *
     * <p>Co y khong dung save(): save() kich hoat @PreUpdate cua AuditableEntity nen
     * moi luot danh gia cua khach lai ghi de updated_by cua cuon sach thanh email khach do
     * - man hinh quan tri hien nguoi sua sai. Day la so lieu dan xuat, khong phai ai sua sach.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Book b
        SET
            b.averageStars = :averageStars,
            b.ratingCount  = :ratingCount
        WHERE b.id = :bookId
    """)
    void updateRatingSummary(
            @Param("bookId") Long bookId,
            @Param("averageStars") Double averageStars,
            @Param("ratingCount") Integer ratingCount
    );

    // Nap kem category de mapper khong phai ban them 1 cau cho moi cuon sach
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    // check exists
    @Query("""
        SELECT
            COUNT(b) > 0
        FROM Book b
        WHERE LOWER(b.name) = LOWER(:name)
            AND LOWER(b.author) = LOWER(:author)
    """)
    boolean existsByNameAndAuthorIgnoreCase(@Param("name") String name, @Param("author") String author);

    @Query("""
        SELECT
            LOWER(CONCAT(b.name, '_', b.author))
        FROM Book b
        WHERE LOWER(CONCAT(b.name, '_', b.author)) IN :keys
    """)
    Set<String> findExistingNamesAndAuthorsIgnoreCase(@Param("keys") Set<String> keys);

    // Tim theo khoa nghiep vu name + author, dung cho import UPDATE/DELETE
    @Query("""
        SELECT
            b
        FROM Book b
        WHERE LOWER(b.name) = LOWER(:name)
            AND LOWER(b.author) = LOWER(:author)
    """)
    Optional<Book> findByNameAndAuthorIgnoreCase(@Param("name") String name, @Param("author") String author);

    // Find all books by book ids
    @Query("""
        SELECT
            b
        FROM Book b
        WHERE b.id IN :bookIds
    """)
    List<Book> findAllByBookIds(@Param("bookIds") Set<Long> bookIds);

    // delete by category id
    void deleteByCategoryId(Long categoryId);
}
