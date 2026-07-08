package com.netease.lofter.tango.impl.web.vo;

import com.netease.lofter.tango.impl.consts.Codes;
import com.netease.yaolu.commons.utils.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 数据结构规范：
 * https://music-rtfm.hz.netease.com/music-arc-docs/overmind-x/intro/api_limit.html
 *
 * @param <T>
 */
@Setter
@Getter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -6732657606425514630L;
    private static final ErrorCode SUCCESS = Codes.Common.OK;

    /**
     * code码 200: 成功，其它: 失败
     */
    private int code;

    /**
     * code 对应的错误信息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;


    public Result() {
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMsg();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        ;
        result.setData(data);
        return result;
    }


    public static <T> Result<T> error(ErrorCode bizCode) {
        return error(bizCode, null);
    }

    public static <T> Result<T> error(ErrorCode bizCode, String errMsg) {
        Result<T> result = success();
        result.setCode(bizCode.getCode());
        result.setMessage(bizCode.getMsg());
        if (null != errMsg) {
            result.setMessage(errMsg);
        }
        return result;
    }

    public static <T> Result<T> genericFail(String errMsg) {
        return error(Codes.Common.ERR_BAD_REQUEST, errMsg);
    }

    public boolean isSuccess() {
        return getCode() == SUCCESS.getCode();
    }
}
