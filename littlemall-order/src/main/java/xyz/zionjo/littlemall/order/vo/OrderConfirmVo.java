package xyz.zionjo.littlemall.order.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderConfirmVo {

    List<MemberAddressVo> address;

    List<OrderItemVo> items;

    Integer integration;

    String orderToken;

    Map<Long, Boolean> stocks;


    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if(items != null){
            for (OrderItemVo item : items) {
                sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return sum;
    }


    public BigDecimal getPayPrice(){
        return getTotal();
    }

    public Integer getCount(){
        Integer count = 0;
        if(items != null){
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }
}
