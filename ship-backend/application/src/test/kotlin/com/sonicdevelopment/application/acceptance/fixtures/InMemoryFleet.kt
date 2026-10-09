package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO

/**
 * The fleet, its Shippings and their Cargo in memory. Stores plain records and rebuilds fresh domain
 * objects on every read, as the JPA adapters do.
 */
class InMemoryFleet(private val catains: InMemoryCatains) :
    ShipRepositoryPort, ShippingRepositoryPort, CargoPersistencePort {

    private data class ShipRecord(
        val shipId: ShipId,
        val shipName: String,
        val catainId: CatainId,
        val inFleet: Boolean,
        val arrivedFrom: HarborName?,
        val cargoAboard: List<Cargo>,
        val incoming: Boolean,
    )

    private data class ShippingRecord(
        val shippingId: ShippingId,
        val shipId: ShipId,
        val state: ShippingState,
        val quote: ShippingQuote?,
        val destinationHarbor: HarborName?,
        val cargo: List<Cargo>,
    )

    private val ships = LinkedHashMap<ShipId, ShipRecord>()
    private val shippings = LinkedHashMap<ShippingId, ShippingRecord>()

    @Synchronized
    fun reset() {
        ships.clear()
        shippings.clear()
    }

    @Synchronized
    override fun saveNewShip(ship: ShipRepositoryPort.InitialShipInformation) {
        catains.findCatainById(ship.catainId) ?: throw IllegalStateException("Unknown Catain ${ship.catainId}")
        // like the adapter, a save replaces the Cargo aboard and the Incoming flag
        ships[ship.shipId] = ShipRecord(
            ship.shipId, ship.shipName, ship.catainId, true, ship.arrivedFrom,
            ship.cargoAboard.map { Cargo(it.id, it.name, it.weight) }, ship.incoming,
        )
    }

    @Synchronized
    override fun getAllShips(): List<Ship> = ships.values.filter { it.inFleet }.map { toShip(it) }

    @Synchronized
    override fun getShipDetails(shipId: ShipId): Ship? = ships[shipId]?.takeIf { it.inFleet }?.let { toShip(it) }

    @Synchronized
    override fun delete(shipId: ShipId): Boolean {
        ships[shipId]?.takeIf { it.inFleet } ?: return false
        shippings.values.removeAll { it.shipId == shipId }
        ships.remove(shipId)
        return true
    }

    @Synchronized
    override fun removeFromFleet(shipId: ShipId) {
        ships[shipId]?.let { ships[shipId] = it.copy(inFleet = false) }
    }

    /** Like the adapter's conditional `UPDATE`: only an Incoming Ship in the fleet is cleared, and only once. */
    @Synchronized
    override fun unloadIncomingShip(shipId: ShipId): Boolean {
        val record = ships[shipId]?.takeIf { it.inFleet && it.incoming } ?: return false
        ships[shipId] = record.copy(incoming = false, cargoAboard = emptyList())
        return true
    }

    @Synchronized
    override fun createShipping(ship: Ship) {
        ships[ship.id] ?: throw IllegalStateException()
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        shippings[shipping.id] = ShippingRecord(shipping.id, ship.id, shipping.shippingState, null, null, emptyList())
    }

    @Synchronized
    override fun updateActiveShipping(ship: Ship) {
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        val record = shippings[shipping.id] ?: throw IllegalStateException()
        shippings[shipping.id] = record.copy(
            state = shipping.shippingState,
            quote = shipping.shippingQuote,
            destinationHarbor = shipping.destinationHarbor,
        )
    }

    @Synchronized
    override fun getShippingInformation(shipId: ShipId, shippingId: ShippingId): ShippingDetailsDTO? {
        val shipping = shippings[shippingId]?.takeIf { it.shipId == shipId } ?: return null
        val ship = ships[shipId] ?: return null
        val catain = catains.findCatainById(ship.catainId) ?: throw IllegalStateException()
        return ShippingDetailsDTO(
            shipId = ship.shipId,
            shippingId = shipping.shippingId,
            catainId = catain.catainId.id,
            catainName = catain.catainName,
            shipName = ship.shipName,
            shippingState = shipping.state,
            shippingQuote = shipping.quote,
            cargo = shipping.cargo.map { CargoDTO(it.id, it.name, it.weight) },
            actualWeight = 0.0f,
            destinationHarbor = shipping.destinationHarbor,
        )
    }

    @Synchronized
    override fun updateCargoLoad(
        cargoLoadInformation: CargoPersistencePort.CargoLoadInformation
    ): CargoPersistencePort.CargoLoadInformation {
        val record = shippings[cargoLoadInformation.shippingId] ?: throw IllegalStateException()
        shippings[record.shippingId] = record.copy(
            cargo = cargoLoadInformation.cargoLoad.map { Cargo(it.id, it.name, it.weight) }
        )
        return cargoLoadInformation
    }

    private fun toShip(record: ShipRecord): Ship {
        val catain = catains.findCatainById(record.catainId) ?: throw IllegalStateException()
        val active = shippings.values.firstOrNull {
            it.shipId == record.shipId && (it.state == ShippingState.PREPARING || it.state == ShippingState.SHIPPING)
        }
        return Ship(
            id = record.shipId,
            name = record.shipName,
            catainId = record.catainId,
            catainName = catain.catainName,
            arrivedFrom = record.arrivedFrom,
            activeShipping = active?.let {
                Shipping(it.shippingId, it.quote, it.state, it.destinationHarbor)
            },
            cargoLoad = active?.cargo?.map { Cargo(it.id, it.name, it.weight) }?.toMutableList() ?: mutableListOf(),
            cargoAboard = record.cargoAboard.map { Cargo(it.id, it.name, it.weight) },
            incoming = record.incoming,
        )
    }
}
