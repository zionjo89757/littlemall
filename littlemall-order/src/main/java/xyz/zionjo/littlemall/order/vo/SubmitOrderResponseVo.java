package xyz.zionjo.littlemall.order.vo;

import lombok.Data;
import xyz.zionjo.littlemall.order.entity.OrderEntity;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;

}
