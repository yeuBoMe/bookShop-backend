package com.fullstack.bookShop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "order_details",
        indexes = {
                @Index(name = "idx_order_details_order_id", columnList = "order_id"),
                @Index(name = "idx_order_details_book_id", columnList = "book_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_details_id_seq")
    @SequenceGenerator(name = "order_details_id_seq", sequenceName = "order_details_id_seq", allocationSize = 50)
    private Long id;

    private int quantity;

    private String bookName;

    @ManyToOne()
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
