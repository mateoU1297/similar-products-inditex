package com.similarproducts.inditex.errors;

public class UpstreamException extends RuntimeException {
    public UpstreamException() { super(); }
    public UpstreamException(String msg) { super(msg); }
    public UpstreamException(String msg, Throwable cause) { super(msg, cause); }
}
