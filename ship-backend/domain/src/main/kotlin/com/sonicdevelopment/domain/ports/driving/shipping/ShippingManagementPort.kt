package com.sonicdevelopment.domain.ports.driving.shipping


interface ShippingManagementPort {
    fun createShipping(shippingInformation: ShippingCreationDataDTO): ShippingInformationDTO?
}
