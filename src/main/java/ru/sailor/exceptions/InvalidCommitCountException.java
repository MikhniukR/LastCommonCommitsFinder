package ru.sailor.exceptions;

public class InvalidCommitCountException extends GitCommunicationException {

    public InvalidCommitCountException(String message) {
        super(message);
    }

}
