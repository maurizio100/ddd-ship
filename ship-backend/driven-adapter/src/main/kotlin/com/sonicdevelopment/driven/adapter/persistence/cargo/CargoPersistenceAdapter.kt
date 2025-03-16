package com.sonicdevelopment.driven.adapter.persistence.cargo

import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import org.springframework.stereotype.Component

@Component
class CargoPersistenceAdapter(
    private val shippingRepository: ShippingRepository,
    private val cargoRepository: CargoRepository
): CargoPersistencePort {

    override fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation {
        val persistedShipping = shippingRepository.findByShippingId(
            cargoLoadInformation.shippingId.id
        ) ?: throw IllegalStateException()

        persistedShipping.cargoLoad = getCargoData(cargoLoadInformation)
        shippingRepository.save(persistedShipping)

        return cargoLoadInformation
    }

    private fun getCargoData(cargoLoadInformation: CargoLoadInformation) =
        cargoLoadInformation.cargoLoad.mapNotNull {
            cargoRepository.findByCargoId(it.id.id)
        }.toMutableList()
}
