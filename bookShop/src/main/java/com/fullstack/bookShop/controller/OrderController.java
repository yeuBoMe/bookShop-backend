package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Order;
import com.fullstack.bookShop.service.OrderService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiMessage("Create an order")
    @PostMapping
    public ResponseEntity<RestOrderCreateDTO> createOrderRequest(@Valid @RequestBody Order order) {
        Order orderCreated = this.orderService.createOrder(order);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.orderService.convertOrderToRestOrderCreateDTO(orderCreated));
    }

    @ApiMessage("Fetch list orders")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchListOrdersRequest(
            @Filter Specification<Order> spec,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.orderService.fetchListOrders(spec, pageable));
    }

    @ApiMessage("Get an order")
    @GetMapping("/{id}")
    public ResponseEntity<RestOrderViewDTO> getOrderByIdRequest(@PathVariable Long id) {
        Order orderGetById  = this.orderService.getOrderById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.orderService.convertOrderToRestOrderViewDTO(orderGetById));
    }

    @ApiMessage("Update an order")
    @PutMapping("/{id}")
    public ResponseEntity<RestOrderUpdateDTO> updateOrderRequest(
            @PathVariable Long id,
            @Valid @RequestBody Order order
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    @ApiMessage("Delete an order")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderRequest(@PathVariable Long id) {
        this.orderService.deleteOrderById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
