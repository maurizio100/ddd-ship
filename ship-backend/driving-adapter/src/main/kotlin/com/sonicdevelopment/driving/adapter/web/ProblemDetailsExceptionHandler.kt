package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.exception.CargoOutOfStockException
import com.sonicdevelopment.domain.exception.ItemAlreadyLoadedException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Renders errors as RFC 9457 Problem Details. Domain rule violations become `409` with the
 * exception's message, in domain language, as `detail`. The inherited handlers render Spring's own
 * exceptions, including `ResponseStatusException`, the same way.
 */
@RestControllerAdvice
class ProblemDetailsExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ShipTooHeavyException::class)
    fun shipTooHeavy(exception: ShipTooHeavyException) = conflict("Ship too heavy", exception)

    @ExceptionHandler(ItemAlreadyLoadedException::class)
    fun cargoAlreadyLoaded(exception: ItemAlreadyLoadedException) = conflict("Cargo already loaded", exception)

    @ExceptionHandler(CargoOutOfStockException::class)
    fun cargoOutOfStock(exception: CargoOutOfStockException) = conflict("Cargo out of Stock", exception)

    private fun conflict(title: String, exception: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.message).apply { this.title = title }
}
