package ru.sailor.exceptions;

import java.io.IOException;

public class GitCommunicationException extends IOException {

    public GitCommunicationException(String message) {
        super(message);
    }

}
