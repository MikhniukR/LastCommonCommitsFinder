package ru.sailor.exceptions;

public class InvalidAuthTokenException extends GitCommunicationException {

    public InvalidAuthTokenException(String message) {
        super(message);
    }

}
