package cn.knet.suggest;

import cn.knet.domain.vo.APIResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class BindExceptionHanlder {


    @ExceptionHandler(BindException.class)
    public APIResult handleBindException(BindException ex) {
        FieldError fieldError = ex.getFieldError();
        return APIResult.error(1002,fieldError.getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public APIResult handleBindException(IllegalArgumentException ex) {
        return APIResult.error(1003,ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public APIResult handleBindException(NoHandlerFoundException ex) {
        return APIResult.error(1006,ex.getMessage());
    }
}