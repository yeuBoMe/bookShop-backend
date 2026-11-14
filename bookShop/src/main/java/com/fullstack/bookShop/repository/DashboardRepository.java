package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

/**
 * Truy van tong hop cho man hinh thong ke.
 *
 * <p>Dung native query vi day la cac phep gom nhom thuan doc: khong can nap entity,
 * khong can persistence context. Ket qua do thang vao projection.
 *
 * <p>Chi tinh doanh thu tren don SUCCESS va PAID - don PENDING/UNPAID/CANCEL chua phai tien thuc.
 */
public interface DashboardRepository extends Repository<Order, Long> {

    @Query(value = """
        SELECT
            COALESCE(SUM(o.total_price), 0) AS revenue,
            COUNT(*)                        AS orderCount
        FROM orders o
        WHERE o.status IN ('SUCCESS', 'PAID')
            AND o.created_at >= :fromTime
            AND o.created_at <= :toTime
    """, nativeQuery = true)
    RevenueSummaryProjection fetchRevenueSummary(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime
    );

    @Query(value = """
        SELECT
            CAST(o.created_at AS DATE)      AS revenueDay,
            COALESCE(SUM(o.total_price), 0) AS revenue,
            COUNT(*)                        AS orderCount
        FROM orders o
        WHERE o.status IN ('SUCCESS', 'PAID')
            AND o.created_at >= :fromTime
            AND o.created_at <= :toTime
        GROUP BY CAST(o.created_at AS DATE)
        ORDER BY CAST(o.created_at AS DATE)
    """, nativeQuery = true)
    List<RevenuePointProjection> fetchRevenueByDay(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime
    );

    @Query(value = """
        SELECT
            o.status AS status,
            COUNT(*) AS orderCount
        FROM orders o
        WHERE o.created_at >= :fromTime
            AND o.created_at <= :toTime
        GROUP BY o.status
        ORDER BY COUNT(*) DESC
    """, nativeQuery = true)
    List<StatusCountProjection> fetchOrderCountByStatus(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime
    );

    @Query(value = """
        SELECT
            b.id                          AS bookId,
            b.name                        AS bookName,
            b.author                      AS author,
            SUM(od.quantity)              AS soldQuantity,
            SUM(od.quantity * b.price)    AS revenue
        FROM order_details od
        JOIN books b ON b.id = od.book_id
        JOIN orders o ON o.id = od.order_id
        WHERE o.status IN ('SUCCESS', 'PAID')
            AND o.created_at >= :fromTime
            AND o.created_at <= :toTime
        GROUP BY b.id, b.name, b.author
        ORDER BY SUM(od.quantity) DESC
        LIMIT :maxRows
    """, nativeQuery = true)
    List<TopBookProjection> fetchTopSellingBooks(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("maxRows") int maxRows
    );

    @Query(value = """
        SELECT
            o.email                         AS email,
            MAX(o.name)                     AS customerName,
            COUNT(*)                        AS orderCount,
            COALESCE(SUM(o.total_price), 0) AS totalSpent
        FROM orders o
        WHERE o.status IN ('SUCCESS', 'PAID')
            AND o.created_at >= :fromTime
            AND o.created_at <= :toTime
        GROUP BY o.email
        ORDER BY COALESCE(SUM(o.total_price), 0) DESC
        LIMIT :maxRows
    """, nativeQuery = true)
    List<TopCustomerProjection> fetchTopCustomers(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("maxRows") int maxRows
    );

    /**
     * Doanh thu gom theo the loai - so lieu cho bieu do tron ty trong.
     *
     * <p>LEFT JOIN categories vi sach co the chua gan the loai: dung INNER JOIN thi
     * phan doanh thu do bien mat va tong cac lat cat khong con bang doanh thu that.
     *
     * <p>Don gia lay tu books.price (giong widget sach ban chay) vi order_details
     * khong luu gia tai thoi diem dat.
     */
    @Query(value = """
        SELECT
            c.id                       AS categoryId,
            c.name                     AS categoryName,
            SUM(od.quantity)           AS soldQuantity,
            SUM(od.quantity * b.price) AS revenue
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN books b ON b.id = od.book_id
        LEFT JOIN categories c ON c.id = b.category_id
        WHERE o.status IN ('SUCCESS', 'PAID')
            AND o.created_at >= :fromTime
            AND o.created_at <= :toTime
        GROUP BY c.id, c.name
        ORDER BY SUM(od.quantity * b.price) DESC
    """, nativeQuery = true)
    List<CategoryRevenueProjection> fetchRevenueByCategory(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime
    );

    @Query(value = """
        SELECT
            b.id       AS bookId,
            b.name     AS bookName,
            b.author   AS author,
            b.quantity AS quantity
        FROM books b
        WHERE b.quantity <= :threshold
        ORDER BY b.quantity ASC
        LIMIT :maxRows
    """, nativeQuery = true)
    List<LowStockProjection> fetchLowStockBooks(
            @Param("threshold") int threshold,
            @Param("maxRows") int maxRows
    );


    interface RevenueSummaryProjection {
        Double getRevenue();

        Long getOrderCount();
    }

    interface RevenuePointProjection {
        Date getRevenueDay();

        Double getRevenue();

        Long getOrderCount();
    }

    interface StatusCountProjection {
        String getStatus();

        Long getOrderCount();
    }

    interface TopBookProjection {
        Long getBookId();

        String getBookName();

        String getAuthor();

        Long getSoldQuantity();

        Double getRevenue();
    }

    interface TopCustomerProjection {
        String getEmail();

        String getCustomerName();

        Long getOrderCount();

        Double getTotalSpent();
    }

    interface CategoryRevenueProjection {
        Long getCategoryId();

        String getCategoryName();

        Long getSoldQuantity();

        Double getRevenue();
    }

    interface LowStockProjection {
        Long getBookId();

        String getBookName();

        String getAuthor();

        Integer getQuantity();
    }
}
