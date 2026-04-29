package com.kinetic.sports.common.exception;

import lombok.Getter;

@Getter
public class KineticSportsBindException extends RuntimeException {

    private final int code;

    public KineticSportsBindException(String message) {
        super(message);
        this.code = 1;
    }

    public KineticSportsBindException(int code, String message) {
        super(message);
        this.code = code;
    }
}
