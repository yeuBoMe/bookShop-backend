package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.dto.order.RestOrderStatusDTO;
import com.fullstack.bookShop.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.service.OrderService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.security.SecurityUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ApiMessage("Create an order")
    public ResponseEntity<RestOrderCreateDTO> createOrderRequest(@Valid @RequestBody Order order) {
        Order orderCreated = orderService.createOrder(order);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.convertOrderToRestOrderCreateDTO(orderCreated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ApiMessage("Fetch list orders")
    public ResponseEntity<ResultPaginationDTO> fetchListOrdersRequest(
            @Filter Specification<Order> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.fetchListOrders(spec, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @ApiMessage("Get an order")
    public ResponseEntity<RestOrderViewDTO> getOrderByIdRequest(@PathVariable Long id) {
        Order orderGetById = orderService.getOrderById(id);
        return ResponseEntity.ok(orderService.convertOrderToRestOrderViewDTO(orderGetById));
    }

    @PatchMapping("/{id}")
    @ApiMessage("Update an order")
    public ResponseEntity<RestOrderUpdateDTO> updateOrderPartialRequest(
            @PathVariable Long id,
            @Valid @RequestBody RestOrderStatusDTO restOrderStatusDTO
    ) {
        Order order = new Order();
        order.setStatus(restOrderStatusDTO.getStatus());

        Order orderUpdated = orderService.updateOrderPartialById(id, order);
        return ResponseEntity.ok(orderService.convertOrderToRestOrderUpdateDTO(orderUpdated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ApiMessage("Delete an order")
    public ResponseEntity<Void> deleteOrderRequest(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    @ApiMessage("Get order history")
    public ResponseEntity<ResultPaginationDTO> getOrderHistoryRequest(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLoginEmail().orElse(null);
        return ResponseEntity.ok(orderService.fetchListOrdersHistory(email, pageable));
    }
}
