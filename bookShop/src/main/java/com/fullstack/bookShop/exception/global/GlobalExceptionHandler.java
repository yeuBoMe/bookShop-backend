package com.fullstack.bookShop.exception.global;

import com.fullstack.bookShop.dto.response.global.GlobalResponse;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.exception.custom.PermissionException;
import com.fullstack.bookShop.util.constant.CommonMessage;
import jakarta.mail.MessagingException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Object>> globalExceptionHandler(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, CommonMessage.SERVER_ERROR);
    }

    // Handle authentication errors (wrong username/password)
    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class // username fit but wrong password
    })
    public ResponseEntity<GlobalResponse<Object>> authenticationExceptionHandler(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, CommonMessage.GENERAL_ERROR);
    }

    // Handle authorization denied
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<GlobalResponse<Object>> authorizationDeniedExceptionHandler(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, CommonMessage.SERVER_ERROR);
    }

    // Handle resource not found errors
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<GlobalResponse<Object>> noSuchElementExceptionHandler(NoSuchElementException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, CommonMessage.GENERAL_ERROR);
    }

    // Handle not have permission
    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<GlobalResponse<Object>> permissionExceptionHandler(PermissionException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, CommonMessage.FORBIDDEN_ACCESS);
    }

    // Handle validation errors or request body in controller
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Object>> methodArgumentNotValidExceptionHandler(
            MethodArgumentNotValidException ex
    ) {
        return buildValidationErrorResponse(ex);
    }

    // Handle 404 not found or invalid URLs
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalResponse<Object>> noResourceFoundExceptionHandler(NoResourceFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, CommonMessage.RESOURCE_NOT_FOUND);
    }

    // Handle invalid database API usage errors
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<GlobalResponse<Object>> invalidDataAccessApiUsageExceptionHandler(
            InvalidDataAccessApiUsageException ex
    ) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle null pointer errors
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<GlobalResponse<Object>> nullPointerExceptionHandler(NullPointerException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, CommonMessage.GENERAL_ERROR);
    }

    // Handle illegal state errors
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GlobalResponse<Object>> illegalStateExceptionHandler(IllegalStateException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle illegal argument errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponse<Object>> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle missing cookie errors
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<GlobalResponse<Object>> missingRequestCookieExceptionHandler(
            MissingRequestCookieException ex
    ) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle missing multipart/form-data part errors
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<GlobalResponse<Object>> missingServletRequestPartExceptionHandler(
            MissingServletRequestPartException ex
    ) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle URI syntax invalid (chuỗi folder ko đúng cú pháp)
    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<GlobalResponse<Object>> uRISyntaxExceptionHandler(URISyntaxException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle IO error during file operations (Lỗi hệ thống tệp)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<GlobalResponse<Object>> iOExceptionHandler(IOException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, CommonMessage.GENERAL_ERROR);
    }

    // Handle file not found exception
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<GlobalResponse<Object>> fileNotFoundExceptionHandler(FileNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, CommonMessage.FILE_UPLOAD_FAILED);
    }

    // Handle file upload exception
    @ExceptionHandler(FileException.class)
    public ResponseEntity<GlobalResponse<Object>> storageExceptionHandler(FileException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.FILE_UPLOAD_FAILED);
    }

    // Handle missing request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalResponse<Object>> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, CommonMessage.GENERAL_ERROR);
    }

    // Handle lost connection SMTP, wrong authentication, timeout...
    @ExceptionHandler(MailException.class)
    public ResponseEntity<GlobalResponse<Object>> mailExceptionHandler(MailException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, CommonMessage.GENERAL_ERROR);
    }

    // Handle set email not valid, wrong encoding...
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<GlobalResponse<Object>> messagingExceptionHandler(MessagingException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, CommonMessage.GENERAL_ERROR);
    }

    private ResponseEntity<GlobalResponse<Object>> buildErrorResponse(
            Exception ex,
            HttpStatus httpStatus,
            String commonMessage
    ) {
        GlobalResponse<Object> globalResponse = new GlobalResponse<>();
        globalResponse.setMessage(ex.getMessage());
        globalResponse.setStatusCode(httpStatus.value());
        globalResponse.setError(commonMessage);
        return ResponseEntity
                .status(httpStatus)
                .body(globalResponse);
    }

    private ResponseEntity<GlobalResponse<Object>> buildValidationErrorResponse(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        final List<FieldError> fieldErrorList = bindingResult.getFieldErrors();

        List<String> errorList = fieldErrorList.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        GlobalResponse<Object> globalResponse = new GlobalResponse<>();
        globalResponse.setError(ex.getBody().getDetail());
        globalResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        globalResponse.setMessage(
                errorList.size() > 1
                        ? errorList
                        : errorList.getFirst()
        );

        return ResponseEntity.badRequest().body(globalResponse);
    }
}
