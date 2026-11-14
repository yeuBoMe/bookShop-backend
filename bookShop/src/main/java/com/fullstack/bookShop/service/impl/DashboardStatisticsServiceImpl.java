package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.dashboard.DashboardStatisticsResponse;
import com.fullstack.bookShop.repository.DashboardRepository;
import com.fullstack.bookShop.service.interfaces.DashboardStatisticsService;
import com.fullstack.bookShop.util.cache.TtlCache;
import com.fullstack.bookShop.util.constant.CacheKeyConstant;
import com.fullstack.bookShop.util.constant.MessageKeyConstant;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardStatisticsServiceImpl implements DashboardStatisticsService {

    private final DashboardRepository dashboardRepository;

    private final TtlCache ttlCache;

    private final MessageTranslator messageTranslator;

    @Value("${bookshop.dashboard.default-range-days}")
    private int defaultRangeDays;

    @Value("${bookshop.dashboard.top-limit}")
    private int defaultTopLimit;

    @Value("${bookshop.dashboard.low-stock-threshold}")
    private int lowStockThreshold;

    // Doanh thu can tuoi hon bang xep hang nen TTL ngan hon
    @Value("${bookshop.cache.dashboard-revenue-seconds}")
    private long revenueTtlSeconds;

    @Value("${bookshop.cache.dashboard-ranking-seconds}")
    private long rankingTtlSeconds;

    @Value("${bookshop.cache.dashboard-low-stock-seconds}")
    private long lowStockTtlSeconds;

    @Override
    public DashboardStatisticsResponse getStatistics(Instant fromTime, Instant toTime, Integer topLimit) {
        Instant resolvedTo = resolveToTime(toTime);
        Instant resolvedFrom = resolveFromTime(fromTime, resolvedTo);

        return new DashboardStatisticsResponse(
                resolvedFrom,
                resolvedTo,
                getRevenueSummary(resolvedFrom, resolvedTo),
                getRevenueByDay(resolvedFrom, resolvedTo),
                getOrdersByStatus(resolvedFrom, resolvedTo),
                getTopSellingBooks(resolvedFrom, resolvedTo, topLimit),
                getTopCustomers(resolvedFrom, resolvedTo, topLimit),
                getRevenueByCategory(resolvedFrom, resolvedTo, topLimit),
                getLowStockBooks(topLimit)
        );
    }

    @Override
    public DashboardStatisticsResponse.RevenueSummary getRevenueSummary(Instant fromTime, Instant toTime) {
        Instant to = resolveToTime(toTime);
        Instant from = resolveFromTime(fromTime, to);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_REVENUE + rangeKey(from, to),
                revenueTtlSeconds,
                () -> buildRevenueSummary(from, to)
        );
    }

    @Override
    public List<DashboardStatisticsResponse.RevenuePoint> getRevenueByDay(Instant fromTime, Instant toTime) {
        Instant to = resolveToTime(toTime);
        Instant from = resolveFromTime(fromTime, to);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_REVENUE_BY_DAY + rangeKey(from, to),
                revenueTtlSeconds,
                () -> buildRevenueByDay(from, to)
        );
    }

    @Override
    public List<DashboardStatisticsResponse.StatusCount> getOrdersByStatus(Instant fromTime, Instant toTime) {
        Instant to = resolveToTime(toTime);
        Instant from = resolveFromTime(fromTime, to);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_ORDER_STATUS + rangeKey(from, to),
                revenueTtlSeconds,
                () -> buildOrdersByStatus(from, to)
        );
    }

    @Override
    public List<DashboardStatisticsResponse.TopBook> getTopSellingBooks(
            Instant fromTime,
            Instant toTime,
            Integer topLimit
    ) {
        Instant to = resolveToTime(toTime);
        Instant from = resolveFromTime(fromTime, to);
        int limit = resolveTopLimit(topLimit);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_TOP_BOOKS + rangeKey(from, to) + ":" + limit,
                rankingTtlSeconds,
                () -> buildTopSellingBooks(from, to, limit)
        );
    }

    @Override
    public List<DashboardStatisticsResponse.TopCustomer> getTopCustomers(
            Instant fromTime,
            Instant toTime,
            Integer topLimit
    ) {
        Instant to = resolveToTime(toTime);
        Instant from = resolveFromTime(fromTime, to);
        int limit = resolveTopLimit(topLimit);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_TOP_CUSTOMERS + rangeKey(from, to) + ":" + limit,
                rankingTtlSeconds,
                () -> buildTopCustomers(from, to, limit)
        );
    }

    @Override
    public List<DashboardStatisticsResponse.CategoryRevenueShare> getRevenueByCategory(
            Instant fromTime,
            Instant toTime,
            Integer topLimit
    ) {
        Instant to = resolveToTime(toTime);
        Instant from = resolveFromTime(fromTime, to);
        int limit = resolveTopLimit(topLimit);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_REVENUE_BY_CATEGORY + rangeKey(from, to) + ":" + limit,
                rankingTtlSeconds,
                () -> buildRevenueByCategory(from, to, limit)
        );
    }

    @Override
    public List<DashboardStatisticsResponse.LowStockBook> getLowStockBooks(Integer topLimit) {
        int limit = resolveTopLimit(topLimit);

        return ttlCache.getOrLoad(
                CacheKeyConstant.DASHBOARD_LOW_STOCK + limit,
                lowStockTtlSeconds,
                () -> buildLowStockBooks(limit)
        );
    }

    private Instant resolveToTime(Instant toTime) {
        return toTime != null ? toTime : Instant.now();
    }

    private Instant resolveFromTime(Instant fromTime, Instant toTime) {
        return fromTime != null ? fromTime : toTime.minus(defaultRangeDays, ChronoUnit.DAYS);
    }

    private int resolveTopLimit(Integer topLimit) {
        return topLimit != null && topLimit > 0 ? topLimit : defaultTopLimit;
    }

    /**
     * Lam tron moc thoi gian ve phut khi dat key cache.
     * De nguyen mili-giay thi moi request co mot key rieng va cache khong bao gio trung.
     */
    private String rangeKey(Instant fromTime, Instant toTime) {
        return fromTime.truncatedTo(ChronoUnit.MINUTES) + "_" + toTime.truncatedTo(ChronoUnit.MINUTES);
    }

    private DashboardStatisticsResponse.RevenueSummary buildRevenueSummary(Instant fromTime, Instant toTime) {
        var summary = dashboardRepository.fetchRevenueSummary(fromTime, toTime);

        double totalRevenue = summary != null && summary.getRevenue() != null ? summary.getRevenue() : 0.0;
        long orderCount = summary != null && summary.getOrderCount() != null ? summary.getOrderCount() : 0L;
        double averageOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        return new DashboardStatisticsResponse.RevenueSummary(totalRevenue, orderCount, averageOrderValue);
    }

    private List<DashboardStatisticsResponse.RevenuePoint> buildRevenueByDay(Instant fromTime, Instant toTime) {
        return dashboardRepository.fetchRevenueByDay(fromTime, toTime)
                .stream()
                .map(point -> new DashboardStatisticsResponse.RevenuePoint(
                        point.getRevenueDay().toLocalDate(),
                        point.getRevenue() != null ? point.getRevenue() : 0.0,
                        point.getOrderCount() != null ? point.getOrderCount() : 0L
                ))
                .toList();
    }

    private List<DashboardStatisticsResponse.StatusCount> buildOrdersByStatus(Instant fromTime, Instant toTime) {
        return dashboardRepository.fetchOrderCountByStatus(fromTime, toTime)
                .stream()
                .map(status -> new DashboardStatisticsResponse.StatusCount(
                        status.getStatus(),
                        status.getOrderCount() != null ? status.getOrderCount() : 0L
                ))
                .toList();
    }

    private List<DashboardStatisticsResponse.TopBook> buildTopSellingBooks(
            Instant fromTime,
            Instant toTime,
            int topLimit
    ) {
        return dashboardRepository.fetchTopSellingBooks(fromTime, toTime, topLimit)
                .stream()
                .map(book -> new DashboardStatisticsResponse.TopBook(
                        book.getBookId(),
                        book.getBookName(),
                        book.getAuthor(),
                        book.getSoldQuantity() != null ? book.getSoldQuantity() : 0L,
                        book.getRevenue() != null ? book.getRevenue() : 0.0
                ))
                .toList();
    }

    private List<DashboardStatisticsResponse.TopCustomer> buildTopCustomers(
            Instant fromTime,
            Instant toTime,
            int topLimit
    ) {
        return dashboardRepository.fetchTopCustomers(fromTime, toTime, topLimit)
                .stream()
                .map(customer -> new DashboardStatisticsResponse.TopCustomer(
                        customer.getEmail(),
                        customer.getCustomerName(),
                        customer.getOrderCount() != null ? customer.getOrderCount() : 0L,
                        customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0
                ))
                .toList();
    }

    /**
     * Dung so lieu cho bieu do tron: giu {@code topLimit} the loai lon nhat, phan con lai gop vao mot lat "Khac".
     *
     * <p>Chia nho qua thi bieu do tron thanh mot vong tron day vach khong doc duoc,
     * ma bo han phan duoi thi tong cac lat khong con la 100% doanh thu.
     */
    private List<DashboardStatisticsResponse.CategoryRevenueShare> buildRevenueByCategory(
            Instant fromTime,
            Instant toTime,
            int topLimit
    ) {
        List<DashboardRepository.CategoryRevenueProjection> rows =
                dashboardRepository.fetchRevenueByCategory(fromTime, toTime);

        double totalRevenue = rows.stream()
                .mapToDouble(row -> row.getRevenue() != null ? row.getRevenue() : 0.0)
                .sum();

        if (totalRevenue <= 0) {
            return List.of();
        }

        List<DashboardStatisticsResponse.CategoryRevenueShare> slices = new ArrayList<>();
        for (int index = 0; index < Math.min(topLimit, rows.size()); index++) {
            DashboardRepository.CategoryRevenueProjection row = rows.get(index);
            double revenue = row.getRevenue() != null ? row.getRevenue() : 0.0;

            slices.add(new DashboardStatisticsResponse.CategoryRevenueShare(
                    row.getCategoryId(),
                    resolveCategoryName(row.getCategoryName()),
                    revenue,
                    row.getSoldQuantity() != null ? row.getSoldQuantity() : 0L,
                    toPercentage(revenue, totalRevenue)
            ));
        }

        appendOthersSlice(slices, rows, topLimit, totalRevenue);
        return slices;
    }

    private void appendOthersSlice(
            List<DashboardStatisticsResponse.CategoryRevenueShare> slices,
            List<DashboardRepository.CategoryRevenueProjection> rows,
            int topLimit,
            double totalRevenue
    ) {
        if (rows.size() <= topLimit) {
            return;
        }

        double othersRevenue = 0.0;
        long othersSold = 0L;
        for (int index = topLimit; index < rows.size(); index++) {
            DashboardRepository.CategoryRevenueProjection row = rows.get(index);
            othersRevenue += row.getRevenue() != null ? row.getRevenue() : 0.0;
            othersSold += row.getSoldQuantity() != null ? row.getSoldQuantity() : 0L;
        }

        slices.add(new DashboardStatisticsResponse.CategoryRevenueShare(
                null,
                messageTranslator.toLocale(MessageKeyConstant.DASHBOARD_CATEGORY_OTHERS, rows.size() - topLimit),
                othersRevenue,
                othersSold,
                toPercentage(othersRevenue, totalRevenue)
        ));
    }

    /** Sach chua gan the loai van sinh ra doanh thu that, phai co nhan rieng chu khong bo di. */
    private String resolveCategoryName(String categoryName) {
        return StringUtils.hasValue(categoryName)
                ? categoryName
                : messageTranslator.toLocale(MessageKeyConstant.DASHBOARD_CATEGORY_UNCLASSIFIED);
    }

    private double toPercentage(double part, double total) {
        return Math.round(part / total * 1000.0) / 10.0;
    }

    private List<DashboardStatisticsResponse.LowStockBook> buildLowStockBooks(int topLimit) {
        return dashboardRepository.fetchLowStockBooks(lowStockThreshold, topLimit)
                .stream()
                .map(book -> new DashboardStatisticsResponse.LowStockBook(
                        book.getBookId(),
                        book.getBookName(),
                        book.getAuthor(),
                        book.getQuantity() != null ? book.getQuantity() : 0
                ))
                .toList();
    }
}
