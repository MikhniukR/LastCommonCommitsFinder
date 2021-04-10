package ru.sailor.exceptions;

public class RepositoryNotFoundException extends GitCommunicationException {

    public RepositoryNotFoundException(String message) {
        super(message);
    }

}
