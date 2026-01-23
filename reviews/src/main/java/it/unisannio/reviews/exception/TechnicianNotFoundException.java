package it.unisannio.reviews.exception;

public class TechnicianNotFoundException extends RuntimeException{
    public TechnicianNotFoundException(String message) {
        super(message);
    }
}