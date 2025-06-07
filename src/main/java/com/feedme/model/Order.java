package com.feedme.model;

import com.feedme.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order implements Comparable<Order> {
    private final int id;
    private final boolean isVip;
    private OrderStatus orderStatus;

    public Order(int id, boolean isVip) {
        this.id = id;
        this.isVip = isVip;
        this.orderStatus = OrderStatus.PENDING;
    }

    @Override
    public int compareTo(Order other) {
        return Integer.compare(this.id, other.id);
    }
}
