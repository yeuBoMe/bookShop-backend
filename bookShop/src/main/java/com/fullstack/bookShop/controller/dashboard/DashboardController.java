package com.fullstack.bookShop.controller.dashboard;

import com.fullstack.bookShop.dto.response.dashboard.DashboardResponse;
import com.fullstack.bookShop.dto.response.dashboard.DashboardStatisticsResponse;
import com.fullstack.bookShop.service.interfaces.DashboardService;
import com.fullstack.bookShop.service.interfaces.DashboardStatisticsService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private final DashboardStatisticsService dashboardStatisticsService;

    @GetMapping
    @ApiMessage(ActionConstant.DASHBOARD)
    public ResponseEntity<DashboardResponse> getDashboardRequest() {
        return ResponseEntity.ok(dashboardService.getDashBoardInformation());
    }

    /**
     * So lieu thong ke theo khoang thoi gian. Khong truyen from/to thi lay mac dinh trong cau hinh.
     * Endpoint /api/v1/dashboard cu giu nguyen de man hinh dang chay khong bi anh huong.
     */
    @IsAdmin
    @GetMapping("/statistics")
    @ApiMessage(ActionConstant.DASHBOARD_STATISTICS)
    public ResponseEntity<DashboardStatisticsResponse> getDashboardStatisticsRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer topLimit
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getStatistics(from, to, topLimit));
    }

    @IsAdmin
    @GetMapping("/revenue")
    @ApiMessage(ActionConstant.DASHBOARD_REVENUE)
    public ResponseEntity<DashboardStatisticsResponse.RevenueSummary> getRevenueSummaryRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getRevenueSummary(from, to));
    }

    @IsAdmin
    @GetMapping("/revenue-by-day")
    @ApiMessage(ActionConstant.DASHBOARD_REVENUE_BY_DAY)
    public ResponseEntity<List<DashboardStatisticsResponse.RevenuePoint>> getRevenueByDayRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getRevenueByDay(from, to));
    }

    @IsAdmin
    @GetMapping("/order-status")
    @ApiMessage(ActionConstant.DASHBOARD_ORDER_STATUS)
    public ResponseEntity<List<DashboardStatisticsResponse.StatusCount>> getOrdersByStatusRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getOrdersByStatus(from, to));
    }

    @IsAdmin
    @GetMapping("/top-books")
    @ApiMessage(ActionConstant.DASHBOARD_TOP_BOOKS)
    public ResponseEntity<List<DashboardStatisticsResponse.TopBook>> getTopSellingBooksRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer topLimit
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getTopSellingBooks(from, to, topLimit));
    }

    @IsAdmin
    @GetMapping("/top-customers")
    @ApiMessage(ActionConstant.DASHBOARD_TOP_CUSTOMERS)
    public ResponseEntity<List<DashboardStatisticsResponse.TopCustomer>> getTopCustomersRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer topLimit
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getTopCustomers(from, to, topLimit));
    }

    @IsAdmin
    @GetMapping("/revenue-by-category")
    @ApiMessage(ActionConstant.DASHBOARD_REVENUE_BY_CATEGORY)
    public ResponseEntity<List<DashboardStatisticsResponse.CategoryRevenueShare>> getRevenueByCategoryRequest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer topLimit
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getRevenueByCategory(from, to, topLimit));
    }

    @IsAdmin
    @GetMapping("/low-stock")
    @ApiMessage(ActionConstant.DASHBOARD_LOW_STOCK)
    public ResponseEntity<List<DashboardStatisticsResponse.LowStockBook>> getLowStockBooksRequest(
            @RequestParam(required = false) Integer topLimit
    ) {
        return ResponseEntity.ok(dashboardStatisticsService.getLowStockBooks(topLimit));
    }
}
