package com.kjs.wuli3.web.internal.response;

import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.response.ValidationErrorDetails;
import com.kjs.wuli3.web.response.WebResponseProperties;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Builds field-level validation details only for client-fixable request errors.
 */
final class ValidationErrorDetailsFactory {

    private final WebResponseProperties responseProperties;

    ValidationErrorDetailsFactory(final WebResponseProperties responseProperties) {
        this.responseProperties = responseProperties;
    }

    @Nullable
    ValidationErrorDetails detail(final Exception ex) {
        if (!this.responseProperties.isValidationDetailEnabled()) {
            return null;
        }
        return switch (ex) {
            case MethodArgumentNotValidException methodArgumentNotValidException ->
                ValidationErrorDetailsFactory.methodArgumentNotValidDetail(methodArgumentNotValidException);
            case ConstraintViolationException constraintViolationException ->
                ValidationErrorDetailsFactory.constraintViolationDetail(constraintViolationException);
            case MissingServletRequestParameterException missingParameterException ->
                new ValidationErrorDetails(List.of(new ValidationErrorDetails.Item(
                        missingParameterException.getParameterName(),
                        ValidationErrorDetailsFactory.detailMessage(missingParameterException),
                        null)));
            case MethodArgumentTypeMismatchException typeMismatchException ->
                new ValidationErrorDetails(List.of(new ValidationErrorDetails.Item(
                        typeMismatchException.getName(),
                        ValidationErrorDetailsFactory.detailMessage(typeMismatchException),
                        typeMismatchException.getValue())));
            default -> null;
        };
    }

    private static ValidationErrorDetails methodArgumentNotValidDetail(final MethodArgumentNotValidException ex) {
        final List<ValidationErrorDetails.Item> errors = new ArrayList<>();
        for (final FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationErrorDetails.Item(
                    fieldError.getField(),
                    ValidationErrorDetailsFactory.message(fieldError),
                    fieldError.getRejectedValue()));
        }
        for (final ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            errors.add(new ValidationErrorDetails.Item(
                    objectError.getObjectName(), ValidationErrorDetailsFactory.message(objectError), null));
        }
        return new ValidationErrorDetails(errors);
    }

    private static ValidationErrorDetails constraintViolationDetail(final ConstraintViolationException ex) {
        final List<ValidationErrorDetails.Item> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ValidationErrorDetails.Item(
                        ValidationErrorDetailsFactory.lastPathNode(violation.getPropertyPath()),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .toList();
        return new ValidationErrorDetails(errors);
    }

    private static String message(final ObjectError objectError) {
        final String defaultMessage = objectError.getDefaultMessage();
        final String code = objectError.getCode();
        return defaultMessage == null ? ValidationErrorDetailsFactory.detailMessage(code) : defaultMessage;
    }

    private static String detailMessage(final Throwable ex) {
        return ValidationErrorDetailsFactory.detailMessage(ex.getMessage());
    }

    private static String detailMessage(final @Nullable String message) {
        return message == null ? WebErrors.BAD_REQUEST.getMessage() : message;
    }

    private static @Nullable String lastPathNode(final Path path) {
        String name = null;
        for (final Path.Node node : path) {
            name = node.getName();
        }
        return name;
    }
}
