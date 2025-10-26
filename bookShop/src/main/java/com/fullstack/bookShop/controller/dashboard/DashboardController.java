package com.fullstack.bookShop.controller.dashboard;

import com.fullstack.bookShop.domain.dto.dashboard.RestDashboardDTO;
import com.fullstack.bookShop.service.DashboardService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @ApiMessage("Get dashboard info")
    @GetMapping
    public ResponseEntity<RestDashboardDTO> getDashboardInfoRequest(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.dashboardService.getDashBoardInfo());
    }
}
