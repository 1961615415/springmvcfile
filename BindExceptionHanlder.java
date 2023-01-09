package cn.knet.wz.conf;

import cn.knet.domain.vo.APIResult;
import lombok.extern.java.Log;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author xuxiannian
 */
@RestControllerAdvice
@Log
public class BindExceptionHanlder {


    @ExceptionHandler(BindException.class)
    public APIResult handleBindException(BindException ex) {
        ex.printStackTrace();

        FieldError fieldError = ex.getFieldError();
        return APIResult.error(1002, fieldError.getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public APIResult handleBindException(IllegalArgumentException ex) {

        ex.printStackTrace();
        return APIResult.error(1003, ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)

    public APIResult handleBindException(NoHandlerFoundException ex) {
        ex.printStackTrace();

        return APIResult.error(1004, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public APIResult handleBindException(RuntimeException ex) {
        ex.printStackTrace();
        return APIResult.error(1005, ex.getMessage());
    }
}