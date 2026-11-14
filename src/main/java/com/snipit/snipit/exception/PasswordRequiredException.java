package com.snipit.snipit.exception;

public class PasswordRequiredException extends RuntimeException {

    public PasswordRequiredException() {
        super("Password required");
    }
}