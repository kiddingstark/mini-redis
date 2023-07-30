package com.mini.redis.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/11 10:43
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    NO_OPS_SUCCEED(0, "0"),

    NUll(0, "NULL"),

    SUCCEED(1, "1"),

    OK(1, "OK"),

    UNAUTHORIZED(401, "Unauthorized"),

    ERROR(500, "Internal Server Error"),

    CLIENT_LINK_EXCEEDED(ERROR.getCode(), "client link exceeded"),

    CLIENT_NOT_LINK(ERROR.getCode(), "client not link"),

    SELECT_DB_EXCEEDED(ERROR.getCode(), "ERR DB index is out of range"),

    OUT_OF_RANGE(ERROR.getCode(), "ERR value is not an integer or out of range"),

    UNKOWN_COMMAND(ERROR.getCode(), "ERR unknown command `%s`, with args beginning with: "),

    WRONG_NUMBER_ARFUMENTS(ERROR.getCode(), "ERR wrong number of arguments for '%s' command"),

    ;

    private final Integer code;
    private final String msg;

}


