package com.example.smartiot.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String msg) { super(msg); }
}
