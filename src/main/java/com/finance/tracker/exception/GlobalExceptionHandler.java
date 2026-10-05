package com.finance.tracker.exception;

import com.finance.tracker.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HashMap<String, String>> handleGlobalErrorResponse(MethodArgumentNotValidException ex) {
        HashMap<String, String> errorMap = new HashMap<>();

        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            errorMap.put(err.getField(), err.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errorMap);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ErrorDTO handleUserAlreadyExistResponse(UserAlreadyExistsException ex) {
        return new ErrorDTO(409, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ErrorDTO handleUserNotFoundResponse(UserNotFoundException ex) {
        return new ErrorDTO(404, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ErrorDTO handleInvalidCredentialsResponse(InvalidCredentialsException ex) {
        return new ErrorDTO(401, ex.getMessage());
    }
}
