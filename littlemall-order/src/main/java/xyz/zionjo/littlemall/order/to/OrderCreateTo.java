package xyz.zionjo.littlemall.order.to;

import lombok.Data;
import xyz.zionjo.littlemall.order.entity.OrderEntity;
import xyz.zionjo.littlemall.order.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
