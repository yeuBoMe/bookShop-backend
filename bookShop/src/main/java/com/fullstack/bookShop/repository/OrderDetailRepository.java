package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long>, JpaSpecificationExecutor<OrderDetail> {

    // Nap dong hang cua ca trang don trong 1 cau, kem book de lay ten va gia
    @EntityGraph(attributePaths = "book")
    List<OrderDetail> findAllByOrderIdIn(Collection<Long> orderIds);

    // delete orderDetails by order id
    void deleteByOrderId(Long orderId);

    boolean existsByBookId(Long bookId);

    // delete by book
    void deleteByBookId(Long bookId);
}
