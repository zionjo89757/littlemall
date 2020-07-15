package xyz.zionjo.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id;
    private StockDetailTo detail;
}
