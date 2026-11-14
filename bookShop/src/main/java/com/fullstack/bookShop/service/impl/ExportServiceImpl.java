package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.BookSearchRequest;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.service.interfaces.ExportService;
import com.fullstack.bookShop.specification.BookSpecification;
import com.fullstack.bookShop.util.constant.ContentTypeConstant;
import com.fullstack.bookShop.util.constant.MessageKeyConstant;
import com.fullstack.bookShop.util.export.CsvStreamWriter;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportServiceImpl.class);

    private final BookRepository bookRepository;

    private final OrderRepository orderRepository;

    private final MessageTranslator messageTranslator;

    @Value("${bookshop.export.chunk-size}")
    private int chunkSize;

    @Value("${bookshop.export.max-rows}")
    private int maxRows;

    @Value("${bookshop.export.csv-separator}")
    private String csvSeparator;

    @Override
    public void exportBooksToCsv(HttpServletResponse response, BookSearchRequest filter) throws IOException {
        Specification<Book> specification = BookSpecification.hasKeyword(filter.getKeyword())
                .and(BookSpecification.hasCategoryName(filter.getCategoryName()))
                .and(BookSpecification.hasCategoryIn(filter.getCategoryIds()))
                .and(BookSpecification.priceFrom(filter.getMinPrice()))
                .and(BookSpecification.priceTo(filter.getMaxPrice()))
                .and(BookSpecification.hasMinAverageRating(filter.getMinRating()));

        streamCsv(
                response,
                "books.csv",
                List.of("Id", "Name", "Author", "Category", "Price", "Quantity", "Sold", "AverageStars"),
                lastId -> readBookChunk(specification, lastId),
                Book::getId,
                book -> List.of(
                        text(book.getId()),
                        book.getName(),
                        book.getAuthor(),
                        book.getCategory() != null ? book.getCategory().getName() : "",
                        text(book.getPrice()),
                        text(book.getQuantity()),
                        book.getSold() != null ? book.getSold() : "0",
                        text(book.getAverageStars())
                )
        );
    }

    @Override
    public void exportOrdersToCsv(HttpServletResponse response, String status) throws IOException {
        String normalizedStatus = StringUtils.trimUpperBlank(status);

        streamCsv(
                response,
                "orders.csv",
                List.of("Id", "CustomerName", "Email", "Phone", "Address", "PaymentMethod", "TotalPrice", "Status", "CreatedAt"),
                lastId -> readOrderChunk(normalizedStatus, lastId),
                Order::getId,
                order -> List.of(
                        text(order.getId()),
                        order.getName(),
                        order.getEmail(),
                        order.getPhone(),
                        order.getAddress(),
                        order.getPaymentMethod(),
                        text(order.getTotalPrice()),
                        order.getStatus() != null ? order.getStatus().name() : "",
                        text(order.getCreatedAt())
                )
        );
    }

    /**
     * Doc theo lo bang keyset tren id roi ghi ngay tung lo ra response.
     *
     * <p>Khong dung OFFSET vi cang ve cuoi cang cham; khong gom het roi moi ghi vi
     * nhu vay bo nho phinh theo so dong. Bo nho o day chi bang mot lo.
     */
    private <T> void streamCsv(
            HttpServletResponse response,
            String fileName,
            List<String> header,
            Function<Long, List<T>> chunkReader,
            Function<T, Long> idExtractor,
            Function<T, List<String>> rowMapper
    ) throws IOException {
        response.setContentType(ContentTypeConstant.TEXT_CSV);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        long exported = 0;
        long lastId = 0;

        try (CsvStreamWriter writer = new CsvStreamWriter(response.getOutputStream(), csvSeparator)) {
            writer.writeRow(header);

            while (exported < maxRows) {
                List<T> chunk = chunkReader.apply(lastId);
                if (chunk.isEmpty()) {
                    break;
                }

                for (T row : chunk) {
                    writer.writeRow(rowMapper.apply(row));
                    lastId = idExtractor.apply(row);
                    exported++;

                    if (exported >= maxRows) {
                        break;
                    }
                }

                // Day tung lo ra ngay de client thay du lieu chay, khong doi den khi xong het
                writer.flush();
            }

            // Byte dau tien da gui di thi khong doi duoc HTTP status nua, nen bao cat
            // ngay trong file - de nguoi dung nhan ra thay vi tuong da tai du.
            if (exported >= maxRows) {
                writer.writeRow(List.of(truncationNotice(maxRows)));
            }
        }

        if (exported >= maxRows) {
            LOGGER.warn("Export {} cham tran {} dong, phan con lai bi cat", fileName, maxRows);
        }
        LOGGER.info("Export {} xong: {} dong", fileName, exported);
    }

    private List<Book> readBookChunk(Specification<Book> specification, Long lastId) {
        Specification<Book> afterLastId = specification
                .and((root, query, builder) -> builder.greaterThan(root.get("id"), lastId));

        return bookRepository.findBy(
                afterLastId,
                query -> query.sortBy(Sort.by(Sort.Direction.ASC, "id")).limit(chunkSize).all()
        );
    }

    private List<Order> readOrderChunk(String status, Long lastId) {
        Specification<Order> specification = (root, query, builder) ->
                builder.greaterThan(root.get("id"), lastId);

        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status").as(String.class), status));
        }

        return orderRepository.findBy(
                specification,
                query -> query.sortBy(Sort.by(Sort.Direction.ASC, "id")).limit(chunkSize).all()
        );
    }

    /** Dong cuoi file bao ro da bi cat, thay vi de nguoi dung tuong minh tai du du lieu. */
    private String truncationNotice(int limit) {
        return messageTranslator.toLocale(MessageKeyConstant.EXPORT_TRUNCATED, limit);
    }

    private String text(Object value) {
        return value != null ? String.valueOf(value) : "";
    }
}
