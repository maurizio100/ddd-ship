package at.willhaben.domain.ports.driving.shipping


interface ShippingManagementPort {
    fun createShipping(shippingInformation: ShippingCreationDataDTO): ShippingInformationDTO
}
