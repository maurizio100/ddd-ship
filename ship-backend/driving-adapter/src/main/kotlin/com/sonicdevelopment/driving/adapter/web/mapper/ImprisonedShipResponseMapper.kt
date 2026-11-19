package com.sonicdevelopment.driving.adapter.web.mapper

import com.sonicdevelopment.domain.ports.driving.harbor.ImprisonedShipDTO
import com.sonicdevelopment.domain.ports.driving.shipping.FinePaymentDTO
import com.sonicdevelopment.driving.adapter.web.responsemodel.FinePaymentResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ImprisonedShipResponse

fun toImprisonedShipResponse(imprisonedShip: ImprisonedShipDTO) =
    ImprisonedShipResponse(
        shipId = imprisonedShip.id.id,
        name = imprisonedShip.name,
        catain = imprisonedShip.catain,
        homeHarbor = imprisonedShip.homeHarbor.name,
        fine = imprisonedShip.fine.toDecimalString(),
        savingsCoverFine = imprisonedShip.savingsCoverFine,
    )

fun toFinePaymentResponse(payment: FinePaymentDTO) =
    FinePaymentResponse(
        shipId = payment.shipId.id,
        name = payment.shipName,
        fine = payment.fine.toDecimalString(),
        savings = payment.savings.toDecimalString(),
    )
