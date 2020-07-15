package xyz.zionjo.littlemall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {
    private List<CartItemVo> items;
    private Integer countNum;
    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(items != null && items.size()>0){
            for (CartItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        int count = 0;
        if(items != null && items.size()>0){
            for (CartItemVo item : items) {
                count += 1;
            }
        }
        return count;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount= new BigDecimal("0.0");
        if(items != null && items.size()>0){
            for (CartItemVo item : items) {
                if(item.getCheck())
                amount = amount.add(item.getTotalPrice());
            }
        }
        BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }


}
