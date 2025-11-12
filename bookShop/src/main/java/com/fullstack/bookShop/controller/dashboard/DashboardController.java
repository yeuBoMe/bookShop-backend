package com.fullstack.bookShop.controller.dashboard;

import com.fullstack.bookShop.domain.dto.dashboard.RestDashboardDTO;
import com.fullstack.bookShop.service.DashboardService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @ApiMessage("Get dashboard info")
    @GetMapping
    public ResponseEntity<RestDashboardDTO> getDashboardInfoRequest(){
        return ResponseEntity.ok(dashboardService.getDashBoardInfo());
    }
}
