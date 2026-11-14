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
import com.fullstack.bookShop.util.constant.MessageKeyConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.OrderEnum;
import com.fullstack.bookShop.util.enums.StatusEnum;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    // Phai khop quy tac dang chay o FE (cart.service.ts), neu lech thi tong tien hien thi khac tong tien luu
    @Value("${bookshop.order.free-shipping-threshold}")
    private double freeShippingThreshold;

    @Value("${bookshop.order.shipping-fee}")
    private double shippingFee;

    private final MessageTranslator messageTranslator;

    private final OrderMapper orderMapper;

    private final BookService bookService;

    private final EmailService emailService;

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public PaginationResponse fetchAllOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        List<OrderResponse> orderResponseList = buildOrderResponses(orderPage.getContent());

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(orderPage, pageable);
        paginationResponse.setResult(orderResponseList);
        return paginationResponse;
    }

    @Override
    public PaginationResponse fetchListOrdersHistory(String email, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByEmail(email, pageable);
        List<OrderResponse> orderResponseList = buildOrderResponses(orderPage.getContent());

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(orderPage, pageable);
        paginationResponse.setResult(orderResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public Order createOrder(Order orderRequest) {
        List<OrderDetail> orderDetailsFromRequest = orderDetailsOf(orderRequest);
        LOGGER.info("Creating new order, items={}", orderDetailsFromRequest.size());

        if (orderDetailsFromRequest.isEmpty()) {
            throw new IllegalArgumentException(OrderEnum.DETAIL_REQUIRED.getMessageKey());
        }

        Map<Long, Book> bookMap = loadBooksForOrder(orderDetailsFromRequest);
        Map<Long, Integer> quantityByBookId = sumQuantityByBookId(orderDetailsFromRequest);
        validateStockAvailable(bookMap, quantityByBookId);

        // KHONG tin totalPrice client gui: neu tin thi dat hang voi totalPrice = 0 van duoc chap nhan
        orderRequest.setTotalPrice(calculateTotalPrice(bookMap, quantityByBookId));

        Order orderCreated = orderRepository.save(orderRequest);
        List<OrderDetail> orderDetailList = buildListOrderDetail(orderRequest, bookMap);

        orderDetailRepository.saveAll(orderDetailList);
        orderCreated.setDetail(orderDetailList);
        applyStockChange(bookMap, quantityByBookId);

        // send order to email
        OrderResponse orderResponse = orderMapper.toResponse(orderCreated, ActionConstant.CREATE);
        try {
            emailService.sendEmailWithTemplateSync(
                    orderCreated.getEmail(),
                    messageTranslator.toLocale(MessageKeyConstant.EMAIL_ORDER_SUBJECT, orderCreated.getId()),
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
        StatusEnum currentStatus = orderGetById.getStatus();
        StatusEnum targetStatus = orderStatusRequest.getStatus();

        validateStatusTransition(currentStatus, targetStatus);
        applyStockChangeForStatusChange(id, currentStatus, targetStatus);

        orderGetById.setStatus(targetStatus);

        LOGGER.info("Update order partial successfully with id: {}", id);
        return orderRepository.save(orderGetById);
    }

    private void validateStatusTransition(StatusEnum currentStatus, StatusEnum targetStatus) {
        if (currentStatus != null && !currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    OrderEnum.INVALID_STATUS_TRANSITION.getMessageKey(),
                    currentStatus,
                    targetStatus
            ));
        }
    }

    /**
     * Ton kho bi tru ngay luc tao don, nen khi don chuyen sang huy phai tra hang ve kho.
     * Thieu buoc nay thi moi lan huy don la mat hang vinh vien khoi ton kho.
     */
    private void applyStockChangeForStatusChange(Long orderId, StatusEnum currentStatus, StatusEnum targetStatus) {
        boolean wasHoldingStock = currentStatus == null || currentStatus.holdsStock();
        boolean willHoldStock = targetStatus.holdsStock();

        if (wasHoldingStock == willHoldStock) {
            return;
        }

        List<OrderDetail> orderDetails = loadOrderDetails(orderId);
        Map<Long, Integer> quantityByBookId = sumQuantityByBookId(orderDetails);
        Map<Long, Book> bookMap = loadBooksForOrder(orderDetails);

        int direction = willHoldStock ? -1 : 1;
        List<Book> booksToUpdate = new ArrayList<>();

        quantityByBookId.forEach((bookId, quantity) -> {
            Book book = bookMap.get(bookId);
            book.setQuantity(book.getQuantity() + direction * quantity);
            book.setSold(String.valueOf(Math.max(0, parseSold(book.getSold()) - (long) direction * quantity)));
            booksToUpdate.add(book);
        });

        bookService.saveAllBooks(booksToUpdate);
        LOGGER.info("Stock {} for order id={}, books={}",
                willHoldStock ? "re-deducted" : "restored", orderId, booksToUpdate.size());
    }

    /** Tong tien = tien hang + phi ship, dung cong thuc FE dang hien thi cho khach. */
    private double calculateTotalPrice(Map<Long, Book> bookMap, Map<Long, Integer> quantityByBookId) {
        double subtotal = quantityByBookId.entrySet()
                .stream()
                .mapToDouble(entry -> bookMap.get(entry.getKey()).getPrice() * entry.getValue())
                .sum();

        return subtotal + (subtotal >= freeShippingThreshold ? 0.0 : shippingFee);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        messageTranslator.toLocale(OrderEnum.ID_NOT_FOUND.getMessageKey(), id)
                ));
    }

    /**
     * Nap dong hang bang query tuong minh. Da tat open-in-view nen entity tra ve controller
     * la detached: cham vao quan he LAZY o tang view se no LazyInitializationException.
     */
    @Override
    public OrderResponse getOrderResponseById(Long id) {
        Order orderGetById = getOrderById(id);
        return orderMapper.toResponse(orderGetById, ActionConstant.FETCH, loadOrderDetails(id));
    }

    @Override
    public OrderResponse partialUpdateOrderResponseById(Long id, OrderStatusRequest orderStatusRequest) {
        Order orderUpdated = partialUpdateOrderById(id, orderStatusRequest);
        return orderMapper.toResponse(orderUpdated, ActionConstant.UPDATE, loadOrderDetails(id));
    }

    private List<OrderDetail> loadOrderDetails(Long orderId) {
        return orderDetailRepository.findAllByOrderIdIn(Set.of(orderId));
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
                    messageTranslator.toLocale(OrderEnum.ID_NOT_FOUND.getMessageKey(), id)
            );
        }
        orderDetailRepository.deleteByOrderId(id);
        orderRepository.deleteById(id);
        LOGGER.info("Delete order successfully with id: {}", id);
    }

    /**
     * Nap dong hang cua ca trang bang DUNG 1 cau roi gom theo orderId.
     * Truoc day mapper cham lazy collection cua tung don nen 20 don sinh hang chuc cau SQL.
     */
    private List<OrderResponse> buildOrderResponses(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        Set<Long> orderIds = orders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());

        Map<Long, List<OrderDetail>> detailsByOrderId = orderDetailRepository.findAllByOrderIdIn(orderIds)
                .stream()
                .collect(Collectors.groupingBy(orderDetail -> orderDetail.getOrder().getId()));

        return orders.stream()
                .map(order -> orderMapper.toResponse(
                        order,
                        ActionConstant.FETCH,
                        detailsByOrderId.getOrDefault(order.getId(), List.of())
                ))
                .toList();
    }

    private List<OrderDetail> orderDetailsOf(Order orderRequest) {
        return Optional.ofNullable(orderRequest.getDetail()).orElseGet(List::of);
    }

    private Map<Long, Book> loadBooksForOrder(List<OrderDetail> orderDetailsFromRequest) {
        Set<Long> bookIds = orderDetailsFromRequest.stream()
                .filter(detail -> detail.getBook() != null && detail.getBook().getId() != null)
                .map(detail -> detail.getBook().getId())
                .collect(Collectors.toSet());

        Map<Long, Book> bookMap = bookService.getAllBooksByIds(bookIds)
                .stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        for (Long bookId : bookIds) {
            if (!bookMap.containsKey(bookId)) {
                throw new NoSuchElementException(
                        messageTranslator.toLocale(BookEnum.ID_NOT_FOUND.getMessageKey(), bookId)
                );
            }
        }

        return bookMap;
    }

    /** Gộp theo bookId: một đơn có thể có nhiều dòng cùng trỏ về một cuốn sách. */
    private Map<Long, Integer> sumQuantityByBookId(List<OrderDetail> orderDetailsFromRequest) {
        Map<Long, Integer> quantityByBookId = new HashMap<>();

        for (OrderDetail orderDetail : orderDetailsFromRequest) {
            if (orderDetail.getBook() == null || orderDetail.getBook().getId() == null) {
                continue;
            }
            quantityByBookId.merge(orderDetail.getBook().getId(), orderDetail.getQuantity(), Integer::sum);
        }

        return quantityByBookId;
    }

    private void validateStockAvailable(Map<Long, Book> bookMap, Map<Long, Integer> quantityByBookId) {
        quantityByBookId.forEach((bookId, quantity) -> {
            Book book = bookMap.get(bookId);

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        messageTranslator.toLocale(BookEnum.QUANTITY_INVALID.getMessageKey(), book.getName())
                );
            }
            if (book.getQuantity() < quantity) {
                throw new IllegalArgumentException(messageTranslator.toLocale(
                        BookEnum.QUANTITY_NOT_ENOUGH.getMessageKey(),
                        book.getName(),
                        book.getQuantity(),
                        quantity
                ));
            }
        });
    }

    /** Trừ tồn kho + cộng số đã bán, chạy trong cùng transaction với việc lưu đơn. */
    private void applyStockChange(Map<Long, Book> bookMap, Map<Long, Integer> quantityByBookId) {
        List<Book> booksToUpdate = new ArrayList<>();

        quantityByBookId.forEach((bookId, quantity) -> {
            Book book = bookMap.get(bookId);
            book.setQuantity(book.getQuantity() - quantity);
            book.setSold(String.valueOf(parseSold(book.getSold()) + quantity));
            booksToUpdate.add(book);
        });

        bookService.saveAllBooks(booksToUpdate);
    }

    /** sold lưu dạng chuỗi nên dữ liệu cũ có thể không phải số: coi như 0 thay vì vỡ đơn hàng. */
    private long parseSold(String sold) {
        try {
            String normalized = StringUtils.trimBlank(sold);
            return normalized != null ? Long.parseLong(normalized) : 0L;
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private List<OrderDetail> buildListOrderDetail(Order orderRequest, Map<Long, Book> bookMap) {
        return orderDetailsOf(orderRequest).stream()
                .map(orderDetail -> saveOrderDetail(orderRequest, orderDetail, bookMap))
                .toList();
    }

    private OrderDetail saveOrderDetail(
            Order orderRequest,
            OrderDetail orderDetail,
            Map<Long, Book> bookMap
    ) {
        OrderDetail orderDetailNeedToSave = new OrderDetail();

        if (orderDetail.getBook() != null) {
            Long bookId = orderDetail.getBook().getId();
            Book book = bookMap.get(bookId);
            orderDetailNeedToSave.setBook(book);
        }

        orderDetailNeedToSave.setOrder(orderRequest);
        orderDetailNeedToSave.setBookName(orderDetail.getBookName());
        orderDetailNeedToSave.setQuantity(orderDetail.getQuantity());

        return orderDetailNeedToSave;
    }
}
