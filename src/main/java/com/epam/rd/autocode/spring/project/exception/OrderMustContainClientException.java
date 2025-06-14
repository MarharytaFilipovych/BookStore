package com.epam.rd.autocode.spring.project.exception;

public class OrderMustContainClientException extends RuntimeException {
    public OrderMustContainClientException() {
        super("The client's email within order cannot be null!");
    }
}
