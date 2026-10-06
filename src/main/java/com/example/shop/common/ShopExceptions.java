package com.example.shop.common;

public final class ShopExceptions {
    private ShopExceptions() {}

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String message) { super(message); }
    }
}
