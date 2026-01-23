package it.unisannio.saldotecnico.exception;

public class TechnicianNotFoundException extends RuntimeException{
    public TechnicianNotFoundException(String message) {
        super(message);
    }
}