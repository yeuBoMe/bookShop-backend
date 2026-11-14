package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.request.OrderStatusRequest;
import com.fullstack.bookShop.dto.response.OrderResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.OrderMapper;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.service.interfaces.OrderService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderMapper orderMapper;
    
    private final OrderService orderService;

    @IsAdmin
    @GetMapping
    @ApiMessage(ActionConstant.ORDER_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllOrdersRequest(
            @Filter Specification<Order> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.fetchAllOrders(spec, pageable));
    }

    @GetMapping("/history")
    @ApiMessage(ActionConstant.ORDER_HISTORY)
    public ResponseEntity<PaginationResponse> fetchOrdersHistoryRequest(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLoginEmail().orElse(null);
        return ResponseEntity.ok(orderService.fetchListOrdersHistory(email, pageable));
    }
    
    @PostMapping
    @ApiMessage(ActionConstant.ORDER_CREATE)
    public ResponseEntity<OrderResponse> createOrderRequest(@Valid @RequestBody Order orderRequest) {
        Order orderCreated = orderService.createOrder(orderRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderMapper.toResponse(orderCreated, ActionConstant.CREATE));
    }

    @IsAdmin
    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.ORDER_GET)
    public ResponseEntity<OrderResponse> getOrderByIdRequest(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderResponseById(id));
    }

    @PatchMapping("/{id}")
    @ApiMessage(ActionConstant.ORDER_UPDATE_PARTIAL)
    public ResponseEntity<OrderResponse> partialUpdateOrderRequest(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest orderStatusRequest
    ) {
        return ResponseEntity.ok(orderService.partialUpdateOrderResponseById(id, orderStatusRequest));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.ORDER_DELETE)
    public ResponseEntity<Void> deleteOrderRequest(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}
