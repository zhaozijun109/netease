package com.netease.lofter.tango.impl.consts;

import com.netease.yaolu.commons.utils.exception.BizCode;
import com.netease.yaolu.commons.utils.exception.ErrorCode;

public interface Codes {

    interface Common {
        ErrorCode OK = new BizCode(200, "成功");

        ErrorCode ERR_BAD_REQUEST = new BizCode(400, "无效请求");

        ErrorCode ERR_UNAUTHORIZED = new BizCode(401, "未登录");

        ErrorCode ERR_FORBIDDEN = new BizCode(403, "禁止访问");

        ErrorCode ERR_RESOURCE_NOT_FOUND = new BizCode(404, "资源未找到");

        ErrorCode ERR_SERVER_INTERNAL = new BizCode(500, "未知错误");
        // reserved for more common errors
    }

    interface User {
        ErrorCode NO_INVITE = new BizCode(10001, "邀请码功能被停用");

        ErrorCode NOT_REGISTERED = new BizCode(10002, "用户未注册");

        ErrorCode MULTI_CHANNEL = new BizCode(10003, "多平台用户");
        ErrorCode ROLE_INVALID = new BizCode(10005, "无效角色");
    }

    interface Word {
        ErrorCode CONFLICT = new BizCode(20001, "口令已存在");

    }
}
