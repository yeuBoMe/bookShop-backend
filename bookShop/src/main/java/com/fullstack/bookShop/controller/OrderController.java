package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderStatusDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Order;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiMessage("Create an order")
    @PostMapping
    public ResponseEntity<RestOrderCreateDTO> createOrderRequest(@Valid @RequestBody Order order) {
        Order orderCreated = orderService.createOrder(order);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.convertOrderToRestOrderCreateDTO(orderCreated));
    }

    @ApiMessage("Fetch list orders")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchListOrdersRequest(
            @Filter Specification<Order> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.fetchListOrders(spec, pageable));
    }

    @ApiMessage("Get an order")
    @GetMapping("/{id}")
    public ResponseEntity<RestOrderViewDTO> getOrderByIdRequest(@PathVariable Long id) {
        Order orderGetById = orderService.getOrderById(id);
        return ResponseEntity.ok(orderService.convertOrderToRestOrderViewDTO(orderGetById));
    }

    @ApiMessage("Update an order")
    @PatchMapping("/{id}")
    public ResponseEntity<RestOrderUpdateDTO> updateOrderPartialRequest(
            @PathVariable Long id,
            @Valid @RequestBody RestOrderStatusDTO restOrderStatusDTO
    ) {
        Order order = new Order();
        order.setStatus(restOrderStatusDTO.getStatus());

        Order orderUpdated = orderService.updateOrderPartialById(id, order);
        return ResponseEntity.ok(orderService.convertOrderToRestOrderUpdateDTO(orderUpdated));
    }

    @ApiMessage("Delete an order")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderRequest(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiMessage("Get order history")
    @GetMapping("/history")
    public ResponseEntity<ResultPaginationDTO> getOrderHistoryRequest(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLoginEmail().orElse(null);
        return ResponseEntity.ok(orderService.fetchListOrdersHistory(email, pageable));
    }
}
