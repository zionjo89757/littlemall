package xyz.zionjo.common.exception;

public class NoStockException extends RuntimeException {
    public NoStockException(){
        super("没有足够的库存了");
    }

}
