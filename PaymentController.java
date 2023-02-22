package com.mycloud.cloudproviderpayment9002;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pay")
public class PaymentController {
    @Value("${server.port}")
    private String port;
    @RequestMapping("get")
    public String get(){
        return port;
    }
}
