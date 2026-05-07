package com.clubapp.exception;

public abstract class ClubAppException extends RuntimeException {
    public ClubAppException(String message) {
        super(message);
    }
}
