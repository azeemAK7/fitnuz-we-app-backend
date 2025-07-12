package com.fitnuz.project.Exception.CustomException;

public class GeneralAPIException extends RuntimeException {
    private static final long SerialVersionUID = 1L;

    public GeneralAPIException() {
    }

    public GeneralAPIException(String message) {
        super(message);
    }
}
