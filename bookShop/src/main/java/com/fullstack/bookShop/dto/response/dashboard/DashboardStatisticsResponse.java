package com.fullstack.bookShop.dto.response.dashboard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * So lieu thong ke cho man hinh quan tri.
 * Doanh thu chi tinh tren don SUCCESS va PAID - don chua thanh toan hoac da huy khong phai tien thuc.
 */
public record DashboardStatisticsResponse(
        Instant fromTime,
        Instant toTime,
        RevenueSummary revenue,
        List<RevenuePoint> revenueByDay,
        List<StatusCount> ordersByStatus,
        List<TopBook> topSellingBooks,
        List<TopCustomer> topCustomers,
        List<CategoryRevenueShare> revenueByCategory,
        List<LowStockBook> lowStockBooks
) {

    public record RevenueSummary(double totalRevenue, long paidOrderCount, double averageOrderValue) {
    }

    public record RevenuePoint(LocalDate day, double revenue, long orderCount) {
    }

    public record StatusCount(String status, long orderCount) {
    }

    public record TopBook(Long bookId, String name, String author, long soldQuantity, double revenue) {
    }

    public record TopCustomer(String email, String name, long orderCount, double totalSpent) {
    }

    /**
     * Mot lat cat cua bieu do tron ty trong doanh thu theo the loai.
     *
     * <p>{@code percentage} tinh o backend de bieu do, bang so lieu va file xuat ra
     * luon noi cung mot con so - moi noi tu chia lai la moi noi lech nhau mot chut.
     */
    public record CategoryRevenueShare(
            Long categoryId,
            String name,
            double revenue,
            long soldQuantity,
            double percentage
    ) {
    }

    public record LowStockBook(Long bookId, String name, String author, int quantity) {
    }
}
