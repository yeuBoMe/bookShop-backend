package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.model.Rating;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.repository.*;
import com.fullstack.bookShop.service.interfaces.RatingService;
import com.fullstack.bookShop.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Diem trung binh nam san tren books nen moi duong lam doi tap danh gia deu phai tinh lai.
 *
 * <p>Bo qua mot duong la cot diem sai vinh vien: khong con cho nao tu sua lai duoc,
 * ma man hinh khach thi loc va sap xep truc tiep tren cot do.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = RatingSummarySelfTest.READER_EMAIL)
class RatingSummarySelfTest {

    static final String READER_EMAIL = "reader.rating@bookshop.local";

    private static final String OTHER_EMAIL = "other.rating@bookshop.local";

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private Book book;

    private User reader;

    private User other;

    @BeforeEach
    void seed() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Van hoc");
        category = categoryRepository.save(category);

        book = bookRepository.save(newBook(category));
        reader = saveUser(READER_EMAIL, "Nguoi Doc");
        other = saveUser(OTHER_EMAIL, "Nguoi Doc Khac");
    }

    @Test
    @DisplayName("DANH GIA: them danh gia thi diem va so luot tren sach phai cap nhat theo")
    void createMustRefreshSummary() {
        ratingService.createRating(newRating(book, 4));

        Book refreshed = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(refreshed.getAverageStars()).isEqualTo(4.0);
        assertThat(refreshed.getRatingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DANH GIA: diem trung binh lam tron 1 chu so, khong tra ra so 15 chu so thap phan")
    void averageMustBeRoundedToOneDecimal() {
        ratingRepository.save(rawRating(book, other, 4));
        ratingRepository.save(rawRating(book, reader, 5));
        ratingRepository.save(rawRating(book, saveUser("third@bookshop.local", "Ba"), 4));

        refreshViaService();

        Book refreshed = bookRepository.findById(book.getId()).orElseThrow();
        // (4 + 5 + 4) / 3 = 4.333... -> phai la 4.3
        assertThat(refreshed.getAverageStars()).isEqualTo(4.3);
    }

    @Test
    @DisplayName("DANH GIA: xoa danh gia cuoi cung thi sach phai ve 0, khong giu diem cu")
    void deleteMustResetSummaryToZero() {
        Rating created = ratingService.createRating(newRating(book, 5));

        ratingService.deleteRatingById(created.getId());

        Book refreshed = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(refreshed.getAverageStars()).isEqualTo(0.0);
        assertThat(refreshed.getRatingCount()).isZero();
    }

    @Test
    @DisplayName("BUG: xoa nguoi dung lam mat danh gia cua ho nhung diem tren sach van dung nguyen")
    void deleteUserMustRefreshRatedBooks() {
        ratingRepository.save(rawRating(book, reader, 5));
        ratingRepository.save(rawRating(book, other, 1));
        refreshViaService();

        Book beforeDelete = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(beforeDelete.getAverageStars()).isEqualTo(3.0);
        assertThat(beforeDelete.getRatingCount()).isEqualTo(2);

        userService.deleteUserById(other.getId());

        Book afterDelete = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(ratingRepository.countByBookId(book.getId()))
                .as("danh gia cua nguoi bi xoa phai bien mat")
                .isEqualTo(1);
        assertThat(afterDelete.getAverageStars())
                .as("chi con danh gia 5 sao, diem phai la 5.0 chu khong con la 3.0")
                .isEqualTo(5.0);
        assertThat(afterDelete.getRatingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("BUG: khach danh gia lam doi luon nguoi cap nhat cua cuon sach")
    void ratingMustNotOverwriteBookAuditTrail() {
        Book beforeRating = bookRepository.findById(book.getId()).orElseThrow();
        String originalUpdatedBy = beforeRating.getUpdatedBy();

        ratingService.createRating(newRating(book, 5));

        Book afterRating = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(afterRating.getAverageStars()).as("diem van phai duoc cap nhat").isEqualTo(5.0);
        assertThat(afterRating.getUpdatedBy())
                .as("khach danh gia khong phai nguoi sua sach, khong duoc ghi de nguoi cap nhat")
                .isEqualTo(originalUpdatedBy);
    }

    @Test
    @DisplayName("BAO MAT: nguoi danh gia lay tu token, body khong dat ho ten nguoi khac duoc")
    void authorMustComeFromTokenNotRequestBody() {
        Rating forged = newRating(book, 1);
        forged.setUser(other);

        Rating created = ratingService.createRating(forged);

        assertThat(created.getUser().getEmail())
                .as("phai la nguoi dang dang nhap, khong phai user gui kem trong body")
                .isEqualTo(READER_EMAIL);
    }

    /** Goi lai duong tinh diem that su cua he thong thay vi tu set tay trong test. */
    private void refreshViaService() {
        Rating temporary = ratingRepository.save(rawRating(book, saveUser("temp@bookshop.local", "Tam"), 3));
        ratingService.deleteRatingById(temporary.getId());
    }

    private Rating newRating(Book targetBook, int stars) {
        Rating rating = new Rating();
        rating.setBook(targetBook);
        rating.setStars(stars);
        rating.setComment("Nhan xet tu self-test");
        return rating;
    }

    private Rating rawRating(Book targetBook, User user, int stars) {
        Rating rating = newRating(targetBook, stars);
        rating.setUser(user);
        return rating;
    }

    private User saveUser(String email, String fullName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPassword("secret-hash");
            return userRepository.save(user);
        });
    }

    private Book newBook(Category category) {
        Book newBook = new Book();
        newBook.setName("So Do");
        newBook.setAuthor("Vu Trong Phung");
        newBook.setPrice(120_000.0);
        newBook.setQuantity(10);
        newBook.setIntroduceText("Gioi thieu");
        newBook.setDescription("Mo ta");
        newBook.setReader("Doc gia");
        newBook.setCategory(category);
        return newBook;
    }
}
