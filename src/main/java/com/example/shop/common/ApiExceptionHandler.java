package com.example.shop.common;

import com.example.shop.common.ShopExceptions.BusinessRuleException;
import com.example.shop.common.ShopExceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NotFoundException e) {
        return Map.of("error", "NOT_FOUND", "message", e.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(BusinessRuleException e) {
        return Map.of("error", "BUSINESS_RULE", "message", e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(Exception e) {
        return Map.of("error", "VALIDATION_ERROR", "message", "Некорректный запрос");
    }
}
