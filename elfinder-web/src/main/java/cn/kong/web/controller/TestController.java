package cn.kong.web.controller;

import cn.kong.web.exception.BusinessException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping(value = "err")
    public void error(){
        throw new BusinessException(400, "业务异常错误信息");
    }

    @RequestMapping(value = "err2")
    public void error2(){
        throw new NullPointerException("手动抛出异常信息");
    }

    @RequestMapping(value = "err3")
    public int error3(){
        int a = 10 / 0;
        return a;
    }
}