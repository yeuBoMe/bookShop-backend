package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.dto.order.RestOrderDetailViewDTO;
import com.fullstack.bookShop.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.EmailService;
import com.fullstack.bookShop.service.OrderService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final BookService bookService;

    private final EmailService emailService;

    @Transactional
    @Override
    public Order createOrder(Order order) {
        Order orderSaved = orderRepository.save(order);

        List<OrderDetail> orderDetailList = Optional.ofNullable(order.getDetail())
                .map(orderDetails -> orderDetails.stream()
                        .map(orderDetail -> {
                            OrderDetail orderDetailNeedToSave = new OrderDetail();

                            if (orderDetail.getBook() != null) {
                                if (!bookService.checkIdExists(orderDetail.getBook().getId())) {
                                    throw new NoSuchElementException(
                                            "Book with id " + orderDetail.getBook().getId() + " not found!"
                                    );
                                }
                                Book bookGetById = bookService.getBookById(orderDetail.getBook().getId());
                                orderDetailNeedToSave.setBook(bookGetById);
                            }

                            orderDetailNeedToSave.setBookName(orderDetail.getBookName());
                            orderDetailNeedToSave.setOrder(orderSaved);
                            orderDetailNeedToSave.setQuantity(orderDetail.getQuantity());

                            return orderDetailNeedToSave;
                        })
                        .toList())
                .orElse(new ArrayList<>());

        orderDetailRepository.saveAll(orderDetailList);
        orderSaved.setDetail(orderDetailList);

        // send order to email
        RestOrderCreateDTO restOrderCreateDTO = convertOrderToRestOrderCreateDTO(orderSaved);
        try {
            emailService.sendEmailWithTemplateSync(
                    orderSaved.getEmail(),
                    "Xác nhận đơn hàng #" + orderSaved.getId(),
                    "send-order",
                    orderSaved.getName(),
                    restOrderCreateDTO
            );
        } catch (Exception e) {
            System.out.println("Gửi email thất bại >>> " + e.getMessage());
        }

        return orderSaved;
    }

    @Transactional
    @Override
    public Order updateOrderPartialById(Long id, Order orderRequest) {
        Order orderGetById = getOrderById(id);
        if (orderRequest.getStatus() != null) {
            orderGetById.setStatus(orderRequest.getStatus());
        }
        return orderRepository.save(orderGetById);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order with id '" + id + "' not found!"));
    }

    @Override
    public boolean checkIdExists(Long id) {
        return orderRepository.existsById(id);
    }

    @Transactional
    @Override
    public void deleteOrderById(Long id) {
        if (!checkIdExists(id)) {
            throw new NoSuchElementException("Order with id '" + id + "' does not exist!");
        }
        orderDetailRepository.deleteByOrderId(id);
        orderRepository.deleteById(id);
    }

    @Override
    public RestOrderCreateDTO convertOrderToRestOrderCreateDTO(Order order) {
        List<RestOrderDetailViewDTO> orderDetailViewDTOList = Optional.ofNullable(order.getDetail())
                .map(orderDetails -> orderDetails.stream()
                        .map(orderDetail -> new RestOrderDetailViewDTO(
                                orderDetail.getId(),
                                orderDetail.getQuantity(),
                                orderDetail.getBookName(),
                                orderDetail.getBook().getPrice()
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
                order.getStatus(),
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
                                orderDetail.getBookName(),
                                orderDetail.getBook().getPrice()
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
                order.getStatus(),
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
                                orderDetail.getBookName(),
                                orderDetail.getBook().getPrice()
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
                order.getStatus(),
                orderDetailViewDTOList,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCreatedBy(),
                order.getUpdatedBy()
        );
    }

    @Override
    public ResultPaginationDTO fetchListOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<RestOrderViewDTO> restOrderViewDTOList = orderPage.getContent()
                .stream()
                .map(this::convertOrderToRestOrderViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(orderPage, pageable);
        resultPaginationDTO.setResult(restOrderViewDTOList);

        return resultPaginationDTO;
    }

    @Override
    public ResultPaginationDTO fetchListOrdersHistory(String email, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByEmail(email, pageable);

        List<RestOrderViewDTO> restOrderDetailViewDTOList = orderPage.getContent()
                .stream()
                .map(this::convertOrderToRestOrderViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(orderPage, pageable);
        resultPaginationDTO.setResult(restOrderDetailViewDTOList);

        return resultPaginationDTO;
    }
}
