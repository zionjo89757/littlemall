package xyz.zionjo.common.exception;

import org.omg.PortableInterceptor.USER_EXCEPTION;

/**
 * 错误码列表
 *
 * 10 通用
 * 11 商品
 * 15 用户
 * 16 库存
 */
public enum BizCodeEnum {

    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    PHONE_EXIST_EXCEPTION(15001,"手机号码存在异常"),
    USER_EXIST_EXCEPTION(15002,"用户存在异常"),
    NO_STOCK_EXCEPTION(16000,"库存不足"),
    LOGINACCI_PASSWORD_INVALID_EXCEPTION(15003,"账号或密码错误");


    private int code;
    private String msg;
    BizCodeEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
