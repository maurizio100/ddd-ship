package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImage
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.model.values.SailorsCode
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.CatainImageRemotePort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import java.util.UUID

/**
 * The reference data every Harbor has: the literal ids of `V9__same_reference_ids_at_every_harbor.sql`
 * with the names and weights of V2, V3 and V4.
 */
object SeedData {

    private val cargos = listOf(
        Cargo(CargoId(UUID.fromString("e1becd37-94bc-4b58-bd2b-bcbb63613669")), "Ale", 2.0f),
        Cargo(CargoId(UUID.fromString("b3f84d5d-08b1-443d-aaef-bc3707a2adb3")), "Chocolate", 0.8f),
        Cargo(CargoId(UUID.fromString("2c1f9b41-6f81-41d9-8348-13429317bce7")), "Cinnamon", 0.9f),
        Cargo(CargoId(UUID.fromString("fba46bd2-33bd-4a6d-9f5c-ec6685edb7a9")), "Coffee", 1.9f),
        Cargo(CargoId(UUID.fromString("ee1f930a-8eb8-4f28-b8af-67a332f777f1")), "Fruits", 2.3f),
        Cargo(CargoId(UUID.fromString("2b38de1a-113f-4d66-bfe3-27e89d449ed1")), "Leather", 3.1f),
        Cargo(CargoId(UUID.fromString("b31991d5-3fbf-4ca1-a032-0e9eec766e9a")), "Paprika", 1.0f),
        Cargo(CargoId(UUID.fromString("4f629046-b875-43ca-85a2-8f490f3fcb54")), "Planks", 3.3f),
        Cargo(CargoId(UUID.fromString("dbc1c76c-ffd7-4cc6-b209-e64d1407d899")), "Rum", 5.5f),
        Cargo(CargoId(UUID.fromString("7bc669da-f68b-453a-837e-480390f148f4")), "Silk", 2.0f),
        Cargo(CargoId(UUID.fromString("0c63e166-3fa2-4118-85d1-6f514f091805")), "Sugar", 0.7f),
        Cargo(CargoId(UUID.fromString("d8c8aeaf-fb65-40b3-ab87-5be3270d0a43")), "Tobacco", 3.0f),
        Cargo(CargoId(UUID.fromString("89bf7e52-6585-4ef7-b325-22d7ab247164")), "Wheat", 2.7f),
        Cargo(CargoId(UUID.fromString("d863fc29-8f40-4208-b310-3972bf85ec79")), "Wine", 3.3f),
    )

    private val catains = listOf(
        Catain(CatainId(UUID.fromString("4db95d01-a58a-4e34-88d0-cf2c1ccb0d91")), "Furry Jones", CatainImageId("58a6993f8b13de982e86845800d24d19")),
        Catain(CatainId(UUID.fromString("9bd9886b-3e77-44da-af95-4fe28e93ccad")), "Bootsrap Bill", CatainImageId("60b8b2b44f0982a92b536b1d4ea0d1b8")),
        Catain(CatainId(UUID.fromString("e07471dc-a193-4747-aee8-cb4512f8a450")), "Catain Black Whiskers", CatainImageId("1c78951bdf46ddc00611b76089a53999")),
        Catain(CatainId(UUID.fromString("9c8f5d6e-8691-4da3-8553-1a0856e17825")), "Catain Cat Sparrow", CatainImageId("eedaa2e1e5a36dbdd8611ba49de8053a")),
        Catain(CatainId(UUID.fromString("475df0b9-7c53-4b02-93d8-bff646c50240")), "Catain Purrbossa", CatainImageId("eedaa2e1e5a36dbdd8611ba49de8053e")),
    )

    val quotes = listOf(
        "To be successful at sea, we must keep things simple.",
        "There is nothing more enticing, disenchanting, and enslaving than the life at sea.",
        "I wanted freedom, open air, and adventure. I found it on the sea.",
        "Smell the sea and feel the sky. Let your soul and spirit fly.",
        "It is not that life ashore is distasteful to me. But life at sea is better.",
        "The days pass happily with me wherever my ship sails.",
        "Now…bring me that horizon.",
        "The goal is not to sail the boat, but rather to help the boat sail herself.",
        "A ship in the harbor is safe, but that’s not what ships are built for.",
        "Not all treasure is silver and gold, mate.",
        "If one does not know to which port one is sailing, no wind is favorable.",
        "Live in the sunshine, swim the sea, drink the wild air.",
        "Smooth seas do not make skillful sailors.",
        "Any fool can carry on, but a wise man knows how to shorten sail in time.",
        "There are some things you learn best in calm, and some in storm.",
    )

    fun allCargo(): List<Cargo> = cargos.map { Cargo(it.id, it.name, it.weight) }

    fun allCatains(): List<Catain> = catains.map { Catain(it.catainId, it.catainName, it.catainImageId) }

    /** The business id of the seeded Cargo [name]; fails for an unknown name. */
    fun cargoIdOf(name: String): UUID =
        cargos.singleOrNull { it.name == name }?.id?.id ?: error("No seeded Cargo named $name")

    /** Furry Jones, the first seeded Catain. */
    val aCatainId: UUID = catains.first().catainId.id
}

class InMemoryCargoCatalog : CargoQueryPort {
    override fun findAllCargo(): List<Cargo> = SeedData.allCargo()

    override fun findCargo(cargoId: CargoId): Cargo? = SeedData.allCargo().firstOrNull { it.id == cargoId }
}

class InMemoryCatains : CatainRepository {
    override fun findCatainById(catainId: CatainId): Catain? = SeedData.allCatains().firstOrNull { it.catainId == catainId }

    override fun findAllCatains(): List<Catain> = SeedData.allCatains()
}

class InMemoryQuotes : QuoteRepositoryPort {
    override fun getQuoteForSailorsCode(sailorsCode: SailorsCode): ShippingQuote =
        SeedData.quotes.getOrNull(sailorsCode.codeValue)?.let { ShippingQuote(it) } ?: ShippingQuote("Yeah go and sail!")
}

class FakeCatainImages : CatainImageRemotePort {
    override fun findCatainImage(catainImageId: CatainImageId): CatainImage =
        CatainImage(catainImageId.id.toByteArray())
}
