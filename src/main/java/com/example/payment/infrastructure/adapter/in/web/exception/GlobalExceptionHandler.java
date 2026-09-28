package com.example.payment.infrastructure.adapter.in.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "A requisição possui atributos inválidos ou malformados"
        );

        problemDetail.setTitle("Erro de validação de Entrada");
        problemDetail.setType(URI.create("https://api.payment-service.com/errors/invalid-input"));
        problemDetail.setProperty("timestamp", Instant.now());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
               errors.put(error.getField(), error.getDefaultMessage())
        );

        problemDetail.setProperty("invalidFields", errors);
        return problemDetail;
    }
}
