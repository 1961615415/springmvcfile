package com.wmbs.conf;

import com.wmbs.vo.LayResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class BindExceptionHanlder {


    @ExceptionHandler(BindException.class)
    public LayResult<String> handleBindException(BindException ex) {
        FieldError fieldError = ex.getFieldError();
        log.info("参数校验异常：{}", fieldError.getDefaultMessage());
        if (fieldError.getDefaultMessage().startsWith("Failed to convert property value of type")) {
            return LayResult.error(1002, fieldError.getField() + ":参数类型不匹配");
        }
        return LayResult.error(1002, fieldError.getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public LayResult<String> handleBindException(IllegalArgumentException ex) {
        log.info("数据校验异常：{}", ex.getMessage());
        ex.printStackTrace();
        return LayResult.error(1003, ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public LayResult<String> handleBindException(NoHandlerFoundException ex) {
        ex.printStackTrace();
        log.info("绑定异常：{}", ex.getMessage());
        return LayResult.error(1004, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public LayResult<String> handleBindException(RuntimeException ex) {
        log.info("运行时异常：{}", ex.getMessage());
        ex.printStackTrace();
        return LayResult.error(1005, ex.getMessage());
    }
}