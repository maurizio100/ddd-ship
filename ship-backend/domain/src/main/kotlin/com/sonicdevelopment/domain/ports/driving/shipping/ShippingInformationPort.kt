package com.sonicdevelopment.domain.ports.driving.shipping

interface ShippingInformationPort {
    fun getShipping(shippingId: Long): ShippingInformationDTO?
}
