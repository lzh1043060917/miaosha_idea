package com.imooc.miaosha.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;

// 切面
@ControllerAdvice
@ResponseBody // 和controller差不多
public class GlobalExceptionHandler {
    // 拦截所有异常
    @ExceptionHandler(value=Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        if(e instanceof GlobalException) {
            // 强转
            GlobalException ex = (GlobalException)e;
            // 获得CodeMsg
            return Result.error(ex.getCm());
        }else if(e instanceof BindException) {
            // BindException是参数校验错误
            BindException ex = (BindException)e;
            // 有可能是很多错误，所以是列表
            List<ObjectError> errors = ex.getAllErrors();
            // 这里只返回了第一个异常
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else {
            // 服务端异常
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
