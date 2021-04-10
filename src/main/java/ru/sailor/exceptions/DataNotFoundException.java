package ru.sailor.exceptions;

public class DataNotFoundException extends GitCommunicationException {

    public DataNotFoundException(String message) {
        super(message);
    }

}
