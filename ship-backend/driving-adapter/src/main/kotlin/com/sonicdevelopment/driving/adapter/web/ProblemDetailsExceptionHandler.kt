package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.CargoOutOfStockException
import com.sonicdevelopment.domain.exception.NewShippingRefusedException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
import com.sonicdevelopment.domain.exception.UnknownHarborException
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

    @ExceptionHandler(CargoOutOfStockException::class)
    fun cargoOutOfStock(exception: CargoOutOfStockException) = conflict("Cargo out of Stock", exception)

    @ExceptionHandler(UnknownHarborException::class)
    fun unknownHarbor(exception: UnknownHarborException) = conflict("Unknown Destination Harbor", exception)

    @ExceptionHandler(ShippingNotPreparingException::class)
    fun shippingNotPreparing(exception: ShippingNotPreparingException) = conflict("Ship not being prepared", exception)

    @ExceptionHandler(SavingsDoNotCoverException::class)
    fun savingsDoNotCover(exception: SavingsDoNotCoverException) = conflict("Savings do not cover", exception)

    @ExceptionHandler(CargoHasNoPriceException::class)
    fun cargoHasNoPrice(exception: CargoHasNoPriceException) = conflict("Cargo has no Price", exception)

    @ExceptionHandler(NewShippingRefusedException::class)
    fun newShippingRefused(exception: NewShippingRefusedException) = conflict("New Shipping refused", exception)

    @ExceptionHandler(ShipNotIncomingException::class)
    fun shipNotIncoming(exception: ShipNotIncomingException) = conflict("Not an Incoming Ship", exception)

    private fun conflict(title: String, exception: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.message).apply { this.title = title }
}
