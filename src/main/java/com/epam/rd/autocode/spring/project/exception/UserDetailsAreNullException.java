package com.epam.rd.autocode.spring.project.exception;

public class UserDetailsAreNullException extends RuntimeException {
    public UserDetailsAreNullException(String object) {
        super("UserDetails must be either Employee or Client, but was " + object);
    }
}
