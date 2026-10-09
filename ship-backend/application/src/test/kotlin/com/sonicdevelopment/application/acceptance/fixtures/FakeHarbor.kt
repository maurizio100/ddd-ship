package com.sonicdevelopment.application.acceptance.fixtures

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * A transaction manager that begins, commits and rolls back nothing, but runs the real synchronizations,
 * so `afterCommit` still fires and nested `@Transactional` calls join the outer transaction.
 */
class InMemoryTransactionManager : AbstractPlatformTransactionManager() {

    private class Transaction

    override fun doGetTransaction(): Any = Transaction()

    override fun isExistingTransaction(transaction: Any): Boolean =
        TransactionSynchronizationManager.isActualTransactionActive()

    override fun doBegin(transaction: Any, definition: TransactionDefinition) = Unit

    override fun doCommit(status: DefaultTransactionStatus) = Unit

    override fun doRollback(status: DefaultTransactionStatus) = Unit
}

/** One instance of every fake; [reset] puts them all back to a fresh Harbor. */
class FakeDrivenPorts {
    val catalog = InMemoryCargoCatalog()
    val catains = InMemoryCatains()
    val quotes = InMemoryQuotes()
    val catainImages = FakeCatainImages()
    val fleet = InMemoryFleet(catains)
    val stock = InMemoryStock(catalog)
    val prices = InMemoryPrices()
    val savings = InMemorySavings()
    val outbox = InMemoryOutbox()
    val inbox = InMemoryInbox()
    val arrivals = InMemoryArrivals()
    val knownHarbors = InMemoryKnownHarbors()

    fun reset() {
        fleet.reset()
        stock.reset()
        prices.reset()
        savings.reset()
        outbox.reset()
        inbox.reset()
        arrivals.reset()
        knownHarbors.reset()
    }
}

/** The fakes in place of the `driven-adapter` beans. */
@TestConfiguration(proxyBeanMethods = false)
class FakeDrivenPortsConfiguration {

    @Bean
    fun fakeDrivenPorts() = FakeDrivenPorts()

    @Bean
    fun transactionManager(): PlatformTransactionManager = InMemoryTransactionManager()

    @Bean
    fun cargoQueryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.CargoQueryPort = ports.catalog

    @Bean
    fun catainRepository(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.CatainRepository = ports.catains

    @Bean
    fun quoteRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort = ports.quotes

    @Bean
    fun catainImageRemotePort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.CatainImageRemotePort = ports.catainImages

    @Bean
    fun shipRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort = ports.fleet

    @Bean
    fun shippingRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort = ports.fleet

    @Bean
    fun cargoPersistencePort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.CargoPersistencePort = ports.fleet

    @Bean
    fun stockRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.StockRepositoryPort = ports.stock

    @Bean
    fun priceRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort = ports.prices

    @Bean
    fun savingsRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort = ports.savings

    @Bean
    fun shippingOutboxRepository(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository = ports.outbox

    @Bean
    fun harborOutboxRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort = ports.outbox

    @Bean
    fun inboxRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort = ports.inbox

    @Bean
    fun arrivalRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.ArrivalRepositoryPort = ports.arrivals

    @Bean
    fun knownHarborRepositoryPort(ports: FakeDrivenPorts): com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort = ports.knownHarbors
}

/** Removes the whole `driven-adapter` module and `JpaConfiguration` from the component scan. */
class DrivenAdapterExcludeFilter : TypeExcludeFilter() {

    override fun match(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        val name = metadataReader.classMetadata.className
        return name.startsWith("com.sonicdevelopment.driven.adapter.") || name.endsWith(".JpaConfiguration")
    }

    override fun equals(other: Any?): Boolean = other != null && other::class == this::class

    override fun hashCode(): Int = this::class.hashCode()
}

/**
 * The real `ShipBackendApplication` wiring with the driven adapters swapped for in-memory fakes: no
 * DataSource, JPA, Flyway or MinIO, and Kafka unreachable with its listeners idle (ADR-0010).
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
            "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "spring.kafka.bootstrap-servers=localhost:1",
        "spring.kafka.listener.auto-startup=false",
    ]
)
@Import(FakeDrivenPortsConfiguration::class)
@TypeExcludeFilters(DrivenAdapterExcludeFilter::class)
@java.lang.annotation.Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FakeHarborTest
