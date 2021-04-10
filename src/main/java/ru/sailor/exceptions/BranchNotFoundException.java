package ru.sailor.exceptions;

public class BranchNotFoundException extends GitCommunicationException {

    public BranchNotFoundException(String message) {
        super(message);
    }

}
