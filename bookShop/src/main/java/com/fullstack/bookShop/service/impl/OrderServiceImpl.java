package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderDetailViewDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Book;
import com.fullstack.bookShop.domain.entity.Order;
import com.fullstack.bookShop.domain.entity.OrderDetail;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.OrderService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final BookService bookService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            BookService bookService
    ) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.bookService = bookService;
    }

    @Transactional
    @Override
    public Order createOrder(Order order) {
        Order orderSaved = this.orderRepository.save(order);

        List<OrderDetail> orderDetailList = Optional.ofNullable(order.getDetail())
                .map(orderDetails -> orderDetails.stream()
                        .map(orderDetail -> {
                            OrderDetail orderDetailNeedToSave = new OrderDetail();

                            if (orderDetail.getBook() != null) {
                                if (!this.bookService.checkIdExists(orderDetail.getBook().getId())) {
                                    throw new NoSuchElementException("Book with id " + orderDetail.getBook().getId() + " not found");
                                }
                                Book bookGetById = this.bookService.getBookById(orderDetail.getBook().getId());
                                orderDetailNeedToSave.setBook(bookGetById);
                            }

                            orderDetailNeedToSave.setBookName(orderDetail.getBookName());
                            orderDetailNeedToSave.setOrder(orderSaved);
                            orderDetailNeedToSave.setQuantity(orderDetail.getQuantity());
                            return orderDetailNeedToSave;
                        })
                        .toList())
                .orElse(new ArrayList<>());

        this.orderDetailRepository.saveAll(orderDetailList);
        orderSaved.setDetail(orderDetailList);

        Order orderGetById = this.getOrderById(orderSaved.getId());
        return orderGetById;
    }

    @Override
    public Order updateOrderById(Long id, Order order) {
        return null;
    }

    @Override
    public Order getOrderById(Long id) {
        return this.orderRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order with id '" + id + "' not found"));
    }

    @Override
    public boolean checkIdExists(Long id) {
        return this.orderRepository.existsById(id);
    }

    @Override
    public void deleteOrderById(Long id) {
        if(!this.checkIdExists(id)) {
            throw new NoSuchElementException("Order with id '" + id + "' does not exist");
        }
        this.orderDetailRepository.deleteByOrderId(id);
        this.orderRepository.deleteById(id);
    }

    @Override
    public RestOrderCreateDTO convertOrderToRestOrderCreateDTO(Order order) {
        List<RestOrderDetailViewDTO> orderDetailViewDTOList = Optional.ofNullable(order.getDetail())
                .map(orderDetails -> orderDetails.stream()
                        .map(orderDetail -> new RestOrderDetailViewDTO(
                                orderDetail.getId(),
                                orderDetail.getQuantity(),
                                orderDetail.getBookName()
                        ))
                        .toList()
                )
                .orElse(new ArrayList<>());

        return new RestOrderCreateDTO(
                order.getId(),
                order.getName(),
                order.getEmail(),
                order.getPhone(),
                order.getAddress(),
                order.getPaymentMethod(),
                order.getTotalPrice(),
                orderDetailViewDTOList,
                order.getCreatedAt(),
                order.getCreatedBy()
        );
    }

    @Override
    public RestOrderUpdateDTO convertOrderToRestOrderUpdateDTO(Order order) {
        List<RestOrderDetailViewDTO> orderDetailViewDTOList = Optional.ofNullable(order.getDetail())
                .map(orderDetails -> orderDetails.stream()
                        .map(orderDetail -> new RestOrderDetailViewDTO(
                                orderDetail.getId(),
                                orderDetail.getQuantity(),
                                orderDetail.getBookName()
                        ))
                        .toList()
                )
                .orElse(new ArrayList<>());

        return new RestOrderUpdateDTO(
                order.getId(),
                order.getName(),
                order.getEmail(),
                order.getPhone(),
                order.getAddress(),
                order.getPaymentMethod(),
                order.getTotalPrice(),
                orderDetailViewDTOList,
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }

    @Override
    public RestOrderViewDTO convertOrderToRestOrderViewDTO(Order order) {
        List<RestOrderDetailViewDTO> orderDetailViewDTOList = Optional.ofNullable(order.getDetail())
                .map(orderDetails -> orderDetails.stream()
                        .map(orderDetail -> new RestOrderDetailViewDTO(
                                orderDetail.getId(),
                                orderDetail.getQuantity(),
                                orderDetail.getBookName()
                        ))
                        .toList()
                )
                .orElse(new ArrayList<>());

        return new RestOrderViewDTO(
                order.getId(),
                order.getName(),
                order.getEmail(),
                order.getPhone(),
                order.getAddress(),
                order.getPaymentMethod(),
                order.getTotalPrice(),
                orderDetailViewDTOList,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCreatedBy(),
                order.getUpdatedBy()
        );
    }

    @Override
    public ResultPaginationDTO fetchListOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> orderPage = this.orderRepository.findAll(spec, pageable);

        List<RestOrderViewDTO> restOrderViewDTOList = orderPage.getContent()
                .stream()
                .map(this::convertOrderToRestOrderViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(orderPage, pageable);
        resultPaginationDTO.setResult(restOrderViewDTOList);
        return resultPaginationDTO;
    }
}
