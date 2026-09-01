package com.kjs.wuli3.web.internal.advice;

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
 * 仅为客户端可修正的请求错误构建字段级校验详情。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ValidationErrorDetailsFactory {

    private static final String MISSING_PARAMETER = "MissingParameter";
    private static final String TYPE_MISMATCH = "TypeMismatch";
    private static final String MISSING_PARAMETER_MESSAGE = "缺少必填参数";
    private static final String TYPE_MISMATCH_MESSAGE = "参数类型错误";

    private final WebResponseProperties responseProperties;

    public ValidationErrorDetailsFactory(final WebResponseProperties responseProperties) {
        this.responseProperties = responseProperties;
    }

    @Nullable
    public ValidationErrorDetails detail(final Exception ex) {
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
                        ValidationErrorDetailsFactory.MISSING_PARAMETER,
                        ValidationErrorDetailsFactory.MISSING_PARAMETER_MESSAGE)));
            case MethodArgumentTypeMismatchException typeMismatchException ->
                new ValidationErrorDetails(List.of(new ValidationErrorDetails.Item(
                        typeMismatchException.getName(),
                        ValidationErrorDetailsFactory.TYPE_MISMATCH,
                        ValidationErrorDetailsFactory.TYPE_MISMATCH_MESSAGE)));
            default -> null;
        };
    }

    private static ValidationErrorDetails methodArgumentNotValidDetail(final MethodArgumentNotValidException ex) {
        final List<ValidationErrorDetails.Item> errors = new ArrayList<>();
        for (final FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationErrorDetails.Item(
                    fieldError.getField(),
                    ValidationErrorDetailsFactory.code(fieldError),
                    ValidationErrorDetailsFactory.message(fieldError)));
        }
        for (final ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            errors.add(new ValidationErrorDetails.Item(
                    objectError.getObjectName(),
                    ValidationErrorDetailsFactory.code(objectError),
                    ValidationErrorDetailsFactory.message(objectError)));
        }
        return new ValidationErrorDetails(errors);
    }

    private static ValidationErrorDetails constraintViolationDetail(final ConstraintViolationException ex) {
        final List<ValidationErrorDetails.Item> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ValidationErrorDetails.Item(
                        ValidationErrorDetailsFactory.lastPathNode(violation.getPropertyPath()),
                        violation
                                .getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                .getSimpleName(),
                        violation.getMessage()))
                .toList();
        return new ValidationErrorDetails(errors);
    }

    private static String message(final ObjectError objectError) {
        final String defaultMessage = objectError.getDefaultMessage();
        return defaultMessage == null ? WebErrors.BAD_REQUEST.getMessage() : defaultMessage;
    }

    private static String code(final ObjectError objectError) {
        final String code = objectError.getCode();
        return code == null ? WebErrors.BAD_REQUEST.name() : code;
    }

    private static @Nullable String lastPathNode(final Path path) {
        String name = null;
        for (final Path.Node node : path) {
            name = node.getName();
        }
        return name;
    }
}
