package exchange.xyvox.Models;


import exchange.xyvox.Models.Enums.OrderStatusEnum;
import exchange.xyvox.Models.Enums.OrderTypeEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;

@MappedSuperclass
public abstract class Order {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private OrderTypeEnum type;

    @Column(name = "price", nullable = true)
    private Double price;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatusEnum status;

    @Column(name = "orderValue", nullable = false)
    private Double orderValue;

    @Column(name = "createdAt", nullable = false)
    private Timestamp createdAt;

    public Order() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public OrderTypeEnum getType() {
        return type;
    }

    public void setType(OrderTypeEnum type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
