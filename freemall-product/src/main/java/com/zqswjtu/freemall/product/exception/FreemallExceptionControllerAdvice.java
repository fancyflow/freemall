package com.zqswjtu.freemall.product.exception;

import com.zqswjtu.common.exception.BizCodeEnum;
import com.zqswjtu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理com.zqswjtu.freemall.product.controller中出现的所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zqswjtu.freemall.product.controller")
public class FreemallExceptionControllerAdvice {

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{}, 异常类型是{}", e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        result.getFieldErrors().
                forEach(fieldError -> errorMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

//    @ExceptionHandler(value = Throwable.class)
//    public R handleException(Throwable throwable) {
//        log.error("出现未知问题{}, 问题类型是{}", throwable.getMessage(), throwable.getClass());
//        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
//    }
}
