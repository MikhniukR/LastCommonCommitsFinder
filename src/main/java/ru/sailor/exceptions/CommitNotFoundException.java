package ru.sailor.exceptions;

public class CommitNotFoundException extends GitCommunicationException {

    public CommitNotFoundException(String message) {
        super(message);
    }

}
