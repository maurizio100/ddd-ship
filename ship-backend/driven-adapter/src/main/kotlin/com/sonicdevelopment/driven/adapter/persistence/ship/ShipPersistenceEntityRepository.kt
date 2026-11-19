package com.sonicdevelopment.driven.adapter.persistence.ship

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface ShipPersistenceEntityRepository: JpaRepository<ShipPersistenceEntity, Long> {
    fun deleteByShipId(shipId: UUID)
    fun findByShipId(shipId: UUID): ShipPersistenceEntity?
    fun findAllByInFleetTrue(): List<ShipPersistenceEntity>
    fun findByShipIdAndInFleetTrue(shipId: UUID): ShipPersistenceEntity?

    /** The ship in the fleet, read with `SELECT … FOR UPDATE`: its row stays locked until the transaction ends. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findLockedByShipIdAndInFleetTrue(shipId: UUID): ShipPersistenceEntity?

    /**
     * The confiscation of a ship in the fleet: clears the Incoming flag and sets the imprisoned flag; returns the
     * number of updated rows (1 or 0). Clears the persistence context, so a ship loaded before is read again.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        nativeQuery = true,
        value = "UPDATE ships SET ship_incoming = false, ship_imprisoned = true " +
            "WHERE ship_id = :shipId AND ship_in_fleet = true"
    )
    fun confiscate(@Param("shipId") shipId: UUID): Int

    /**
     * Clears the Incoming flag of an Incoming Ship in the fleet; returns the number of updated rows (1 or 0).
     * The `WHERE` is re-checked after a concurrent writer commits, so of two unloadings of the same ship only
     * one updates it. Clears the persistence context, so a ship loaded before is read again with the new flag.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        nativeQuery = true,
        value = "UPDATE ships SET ship_incoming = false " +
            "WHERE ship_id = :shipId AND ship_in_fleet = true AND ship_incoming = true"
    )
    fun unloadIncoming(@Param("shipId") shipId: UUID): Int

    /**
     * Adds [amount] to the Earnings of a ship in the fleet in one atomic step, never read-modify-write; returns
     * the number of updated rows (1 or 0). Clears the persistence context, so a ship loaded before is read again
     * with the new Earnings.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        nativeQuery = true,
        value = "UPDATE ships SET ship_earnings = ship_earnings + :amount WHERE ship_id = :shipId AND ship_in_fleet = true"
    )
    fun addEarnings(@Param("shipId") shipId: UUID, @Param("amount") amount: BigDecimal): Int
}
