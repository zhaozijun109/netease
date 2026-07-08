package com.netease.lofter.tango.impl.aspect;

import com.netease.lofter.tango.impl.consts.Codes;
import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.yaolu.commons.core.HttpUtil;
import com.netease.yaolu.commons.utils.exception.ErrorCodeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestControllerAdvice
public class CommonControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(CommonControllerAdvice.class);
    @Autowired
    private ProfileEnv profileEnv;

    @ExceptionHandler({ErrorCodeException.class})
    public Result handleCodeException(HttpServletRequest request, HttpServletResponse response,
                                      ErrorCodeException e) throws IOException {
        logger.warn("error in uri:{}, ip:{}", HttpUtil.getRequestUrl(request), HttpUtil.getClientIp(request), e);
        return Result.error(e.getErrorCode());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageConversionException.class,
            MethodArgumentTypeMismatchException.class, ServletRequestBindingException.class,
            HttpRequestMethodNotSupportedException.class, HttpMediaTypeException.class,
            MissingPathVariableException.class, BindException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleBindException(HttpServletRequest request, HttpServletResponse response,
                                            Exception e) throws IOException {
        logger.warn("error in uri:{}, ip:{}", HttpUtil.getRequestUrl(request), HttpUtil.getClientIp(request), e);
        Result<Void> result = Result.error(Codes.Common.ERR_BAD_REQUEST);
        //spring @validate注解提示
        if (e instanceof MethodArgumentNotValidException) {
            String errorMsg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError().getDefaultMessage();
            errorMsg = Optional.ofNullable(errorMsg).filter(StringUtils::isNotBlank).orElse(result.getMessage());
            result = Result.genericFail(errorMsg);
        } else if (e instanceof HttpMessageNotReadableException) {
            result = Result.genericFail("数据格式有误");
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            result = Result.genericFail("数据类型不匹配");
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            result = Result.genericFail("Http Method Not Support");
        } else if (e instanceof BindException) {
            BindingResult bindingResult = ((BindException) e).getBindingResult();
            String msg = "参数错误";
            if (!profileEnv.isOnline()) {
                FieldError fieldError = bindingResult.getFieldErrors().get(0);
                msg += ":" + fieldError.getField();
            }
            result = Result.genericFail(msg);
        }
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        logger.error("error in uri:{}, ip:{}", HttpUtil.getRequestUrl(request), HttpUtil.getClientIp(request), e);
        Result<Void> result = Result.error(Codes.Common.ERR_SERVER_INTERNAL);
        if (!profileEnv.isOnline()) {
            if (e instanceof NullPointerException) {
                result = Result.genericFail("空指针");
            }
            if (e instanceof DuplicateKeyException) {
                result = Result.genericFail("唯一键冲突");
            }
            if (e instanceof IllegalArgumentException) {
                result = Result.genericFail(e.getMessage());
            }
            if (e instanceof IndexOutOfBoundsException) {
                result = Result.genericFail("指针越界");
            }
        }
        return result;
    }
}
