package xyz.zionjo.littlemall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {

    private String orderSn;

    private List<OrderItemVo> locks;
}
