package com.fullstack.bookShop.controller.dashboard;

import com.fullstack.bookShop.dto.response.dashboard.DashboardResponse;
import com.fullstack.bookShop.service.interfaces.DashboardService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @ApiMessage(ActionConstant.DASHBOARD)
    public ResponseEntity<DashboardResponse> getDashboardRequest() {
        return ResponseEntity.ok(dashboardService.getDashBoardInformation());
    }
}
