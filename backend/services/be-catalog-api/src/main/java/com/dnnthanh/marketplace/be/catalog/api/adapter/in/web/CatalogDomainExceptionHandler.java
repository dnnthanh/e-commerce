package com.dnnthanh.marketplace.be.catalog.api.adapter.in.web;

import com.dnnthanh.marketplace.be.catalog.api.application.exception.CatalogErrorCode;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.InvalidAttributeValueException;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.InvalidProductException;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.InvalidVariantDefinitionException;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.ProductStateConflictException;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import com.dnnthanh.marketplace.be.platform.web.error.ApiErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps Catalog domain failures to stable localized HTTP error semantics. */
@RestControllerAdvice(
        assignableTypes = {
            ProductPublicController.class,
            ProductPrivateController.class,
            ProductInternalController.class
        })
@RequiredArgsConstructor
public class CatalogDomainExceptionHandler {

    private final ApiErrorFactory errorFactory;

    @ExceptionHandler({
        InvalidProductException.class,
        InvalidAttributeValueException.class,
        InvalidVariantDefinitionException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidDomainInput(
            RuntimeException exception, HttpServletRequest request, Locale locale) {
        return response(CatalogErrorCode.INVALID_PRODUCT, request, locale);
    }

    @ExceptionHandler(ProductStateConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductStateConflict(
            ProductStateConflictException exception, HttpServletRequest request, Locale locale) {
        return response(CatalogErrorCode.PRODUCT_STATE_CONFLICT, request, locale);
    }

    private ResponseEntity<ApiResponse<Void>> response(
            ErrorCode errorCode, HttpServletRequest request, Locale locale) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorFactory.create(errorCode, request, locale)));
    }
}
