package com.similarproducts.inditex.errors;

public class GatewayTimeoutException extends RuntimeException {
    public GatewayTimeoutException() { super(); }
    public GatewayTimeoutException(String msg) { super(msg); }
}
