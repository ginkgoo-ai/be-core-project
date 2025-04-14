package com.ginkgooai.core.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseRuntimeException {
    private static final String TYPE = "https://api.ginkgoo.com/errors/resource-conflict";
    private static final String TITLE = "Resource Conflict";
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public ConflictException(String message) {
        super(TYPE, TITLE, message, STATUS);
    }
}
