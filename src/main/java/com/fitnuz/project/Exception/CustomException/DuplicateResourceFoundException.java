package com.fitnuz.project.Exception.CustomException;

public class DuplicateResourceFoundException extends RuntimeException {

    private static final long SerialVersionUID = 1L;

    public DuplicateResourceFoundException() {
    }

    public DuplicateResourceFoundException(String message) {
        super(message);
    }

}