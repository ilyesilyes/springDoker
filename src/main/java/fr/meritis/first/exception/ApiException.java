package fr.meritis.first.exception;

public class ApiException extends RuntimeException{
    public ApiException(String message) { super(message);}
}
