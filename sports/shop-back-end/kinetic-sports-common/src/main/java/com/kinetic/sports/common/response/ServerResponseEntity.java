package com.kinetic.sports.common.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServerResponseEntity<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static <T> ServerResponseEntity<T> success(T data) {
        ServerResponseEntity<T> entity = new ServerResponseEntity<>();
        entity.setCode(200);
        entity.setMsg("success");
        entity.setData(data);
        return entity;
    }

    public static <T> ServerResponseEntity<T> success() {
        ServerResponseEntity<T> entity = new ServerResponseEntity<>();
        entity.setCode(200);
        entity.setMsg("success");
        return entity;
    }

    public static <T> ServerResponseEntity<T> fail(int code, String msg) {
        ServerResponseEntity<T> entity = new ServerResponseEntity<>();
        entity.setCode(code);
        entity.setMsg(msg);
        return entity;
    }

    public static <T> ServerResponseEntity<T> fail(String msg) {
        ServerResponseEntity<T> entity = new ServerResponseEntity<>();
        entity.setCode(1);
        entity.setMsg(msg);
        return entity;
    }
}
