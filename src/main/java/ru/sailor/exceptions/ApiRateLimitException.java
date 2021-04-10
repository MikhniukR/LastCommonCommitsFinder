package ru.sailor.exceptions;

public class ApiRateLimitException extends GitCommunicationException {

    public ApiRateLimitException(String message) {
        super(message);
    }

}
