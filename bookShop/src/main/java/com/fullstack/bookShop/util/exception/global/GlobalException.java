package com.fullstack.bookShop.util.exception.global;

import com.fullstack.bookShop.domain.response.RestResponse;
import com.fullstack.bookShop.util.exception.custom.PermissionException;
import com.fullstack.bookShop.util.exception.custom.StorageException;
import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    // Handle global exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleGlobalException(Exception exception) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(exception.getMessage());
        response.setError("INTERNAL SERVER ERROR");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    // Handle authentication errors (wrong username/password)
    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<RestResponse<Object>> handleAuthException(Exception ex) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        response.setMessage(ex.getMessage());
        response.setError("Exception occurs...");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    // Handle resource not found errors
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(NoSuchElementException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(restResponse);
    }

    // Handle not hav permission
    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<RestResponse<Object>> handlePermissionException(PermissionException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
        restResponse.setError("Forbidden");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(restResponse);
    }

    // Handle validation errors or request body in controller
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        final List<FieldError> fieldErrorList = bindingResult.getFieldErrors();

        List<String> errorList = fieldErrorList.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError(ex.getBody().getDetail());
        restResponse.setMessage(
                errorList.size() > 1
                        ? errorList
                        : errorList.getFirst()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle validation errors or request body after go through controller
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setError("Validation failed");
        response.setMessage("Invalid request parameters");
        response.setData(errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handle 404 not found or invalid URLs
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        restResponse.setError("404 Not Found. URL may not exist...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(restResponse);
    }

    // Handle invalid database API usage errors
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<RestResponse<Object>> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(restResponse);
    }

    // Handle null pointer errors
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<RestResponse<Object>> handleNullPointerException(NullPointerException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(restResponse);
    }

    // Handle illegal state errors
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RestResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle illegal argument errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle missing cookie errors
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<RestResponse<Object>> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle missing multipart/form-data part errors
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RestResponse<Object>> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle URI syntax invalid (chuỗi folder ko đúng cú pháp)
    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<RestResponse<Object>> handleURISyntaxException(URISyntaxException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle IO error during file operations (Lỗi hệ thống tệp)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<RestResponse<Object>> handleIOException(IOException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(restResponse);
    }

    // Handle file not found exception
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleFileNotFoundException(FileNotFoundException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        restResponse.setError("Exception upload file...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(restResponse);
    }

    // Handle file upload exception
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<RestResponse<Object>> handleStorageException(StorageException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception upload file...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle missing request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle lost connection SMTP, wrong authentication, timeout...
    @ExceptionHandler(MailException.class)
    public ResponseEntity<RestResponse<Object>> handleMailException(MailException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }

    // Handle set email not valid, wrong encoding...
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<RestResponse<Object>> handleMessagingException(MessagingException ex) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError("Exception occurs...");
        restResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(restResponse);
    }
}
