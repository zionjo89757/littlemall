package xyz.zionjo.littlemall.product.execption;

import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.zionjo.common.exception.BizCodeEnum;
import xyz.zionjo.common.utils.R;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "xyz.zionjo.littlemall.product.controller")
public class LittlemallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> map = new HashMap<>();

        bindingResult.getFieldErrors().forEach(item -> {
            map.put(item.getField(),item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data",map);
    }


    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }
}
