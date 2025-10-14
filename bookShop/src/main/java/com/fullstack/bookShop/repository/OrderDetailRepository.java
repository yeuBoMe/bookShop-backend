package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long>, JpaSpecificationExecutor<OrderDetail> {

    // delete orderDetails by order id
    void deleteByOrderId(Long orderId);
}
