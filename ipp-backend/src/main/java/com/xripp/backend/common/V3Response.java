package com.xripp.backend.common;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
public class V3Response<T> implements Serializable {
    private String code;
    private String message;
    private T data;
    private String request_id;
    private String timestamp;

    public static <T> V3Response<T> success(T data) {
        V3Response<T> r = new V3Response<>();
        r.code = "OK";
        r.message = "success";
        r.data = data;
        r.request_id = UUID.randomUUID().toString();
        r.timestamp = Instant.now().toString();
        return r;
    }

    public static <T> V3Response<T> error(String code, String message) {
        V3Response<T> r = new V3Response<>();
        r.code = code;
        r.message = message;
        r.request_id = UUID.randomUUID().toString();
        r.timestamp = Instant.now().toString();
        return r;
    }
}