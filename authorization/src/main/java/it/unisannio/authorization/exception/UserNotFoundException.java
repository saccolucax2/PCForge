package it.unisannio.authorization.exception;

import jakarta.ws.rs.WebApplicationException;

public class UserNotFoundException extends WebApplicationException {
    public UserNotFoundException(String message) {
        super(message);
    }
}