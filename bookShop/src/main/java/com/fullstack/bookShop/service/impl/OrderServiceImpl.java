package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.OrderStatusRequest;
import com.fullstack.bookShop.dto.response.OrderResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.OrderMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.service.interfaces.EmailService;
import com.fullstack.bookShop.service.interfaces.OrderService;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.OrderEnum;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderMapper orderMapper;

    private final BookService bookService;

    private final EmailService emailService;

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public PaginationResponse fetchAllOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> orderResponseList = orderPage.getContent()
                .stream()
                .map(orderMapper::convertOrderToViewResponse)
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(orderPage, pageable);
        paginationResponse.setResult(orderResponseList);
        return paginationResponse;
    }

    @Override
    public PaginationResponse fetchListOrdersHistory(String email, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByEmail(email, pageable);

        List<OrderResponse> orderResponseList = orderPage.getContent()
                .stream()
                .map(orderMapper::convertOrderToViewResponse)
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(orderPage, pageable);
        paginationResponse.setResult(orderResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public Order createOrder(Order orderRequest) {
        LOGGER.info("Creating new order: {}", orderRequest);

        Order orderCreated = orderRepository.save(orderRequest);
        List<OrderDetail> orderDetailList = buildListOrderDetail(orderRequest);

        orderDetailRepository.saveAll(orderDetailList);
        orderCreated.setDetail(orderDetailList);

        // send order to email
        OrderResponse orderResponse = orderMapper.convertOrderToCreateResponse(orderCreated);
        try {
            emailService.sendEmailWithTemplateSync(
                    orderCreated.getEmail(),
                    "Xác nhận đơn hàng #" + orderCreated.getId(),
                    "send-order",
                    orderCreated.getName(),
                    orderResponse
            );
        } catch (Exception e) {
            LOGGER.error("Gửi email thất bại >>> {}", e.getMessage(), e);
        }

        LOGGER.info("Create order successfully with id: {}", orderCreated.getId());
        return orderCreated;
    }

    @Transactional
    @Override
    public Order partialUpdateOrderById(Long id, OrderStatusRequest orderStatusRequest) {
        LOGGER.info("Updating order partial id: {}", id);

        Order orderGetById = getOrderById(id);
        orderGetById.setStatus(orderStatusRequest.getStatus());

        LOGGER.info("Update order partial successfully with id: {}", id);
        return orderRepository.save(orderGetById);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(OrderEnum.ID_NOT_FOUND.getMessage(), id)
                ));
    }

    @Override
    public boolean checkIdExists(Long id) {
        return orderRepository.existsById(id);
    }

    @Transactional
    @Override
    public void deleteOrderById(Long id) {
        LOGGER.info("Deleting order with id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    String.format(OrderEnum.ID_NOT_FOUND.getMessage(), id)
            );
        }
        orderDetailRepository.deleteByOrderId(id);
        orderRepository.deleteById(id);
        LOGGER.info("Delete order successfully with id: {}", id);
    }

    private List<OrderDetail> buildListOrderDetail(Order orderRequest) {
        return Optional.ofNullable(orderRequest.getDetail())
                .orElse(List.of())
                .stream()
                .map(orderDetail -> saveOrderDetail(orderRequest, orderDetail))
                .toList();
    }

    private OrderDetail saveOrderDetail(Order orderRequest, OrderDetail orderDetail) {
        OrderDetail orderDetailNeedToSave = new OrderDetail();

        if (orderDetail.getBook() != null) {
            Long bookId = orderDetail.getBook().getId();
            if (!bookService.checkIdExists(bookId)) {
                throw new NoSuchElementException(
                        String.format(BookEnum.ID_NOT_FOUND.getMessage(), bookId)
                );
            }
            Book bookGetById = bookService.getBookById(bookId);
            orderDetailNeedToSave.setBook(bookGetById);
        }

        orderDetailNeedToSave.setOrder(orderRequest);
        orderDetailNeedToSave.setBookName(orderDetail.getBookName());
        orderDetailNeedToSave.setQuantity(orderDetail.getQuantity());

        return orderDetailNeedToSave;
    }
}
