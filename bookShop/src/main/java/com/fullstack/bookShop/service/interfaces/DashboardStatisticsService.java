package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.response.dashboard.DashboardStatisticsResponse;

import java.time.Instant;
import java.util.List;

/**
 * So lieu thong ke, tach theo tung widget nhu dashboard cua FaceX.
 *
 * <p>Ly do tach: moi widget co nhip thay doi khac nhau. Doanh thu can tuoi (TTL ngan),
 * bang xep hang vai phut moi doi (TTL dai). Gop tat ca vao mot loi goi thi phai lay TTL
 * ngan nhat cho toan bo, tinh lai ca nhung phan gan nhu khong doi.
 *
 * <p>Van giu {@link #getStatistics} de man hinh lay mot lan luc mo trang.
 */
public interface DashboardStatisticsService {

    /** fromTime / toTime / topLimit de null thi lay theo mac dinh trong cau hinh. */
    DashboardStatisticsResponse getStatistics(Instant fromTime, Instant toTime, Integer topLimit);

    DashboardStatisticsResponse.RevenueSummary getRevenueSummary(Instant fromTime, Instant toTime);

    List<DashboardStatisticsResponse.RevenuePoint> getRevenueByDay(Instant fromTime, Instant toTime);

    List<DashboardStatisticsResponse.StatusCount> getOrdersByStatus(Instant fromTime, Instant toTime);

    List<DashboardStatisticsResponse.TopBook> getTopSellingBooks(Instant fromTime, Instant toTime, Integer topLimit);

    List<DashboardStatisticsResponse.TopCustomer> getTopCustomers(Instant fromTime, Instant toTime, Integer topLimit);

    List<DashboardStatisticsResponse.CategoryRevenueShare> getRevenueByCategory(
            Instant fromTime,
            Instant toTime,
            Integer topLimit
    );

    List<DashboardStatisticsResponse.LowStockBook> getLowStockBooks(Integer topLimit);
}
