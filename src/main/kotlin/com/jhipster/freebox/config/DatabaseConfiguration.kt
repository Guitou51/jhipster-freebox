package com.jhipster.freebox.config

import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.h2.H2ConfigurationHelper
import io.r2dbc.spi.ConnectionFactory
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.mapdb.DB
import org.mapdb.DBMaker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.core.env.Environment
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableR2dbcRepositories("com.jhipster.freebox.repository")
@EnableTransactionManagement
class DatabaseConfiguration(private val env: Environment) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server.
     * @throws SQLException if the server failed to start.
     */
    @Throws(SQLException::class)
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    fun h2TCPServer(): Any {
        val port = getValidPortForH2()
        log.debug("H2 database is available on port $port")
        return H2ConfigurationHelper.createServer(port)
    }

    private fun getValidPortForH2(): String {
        var port = Integer.parseInt(env.getProperty("server.port"))
        if (port < 10000) {
            port += 10000
        } else {
            if (port < 63536) {
                port += 2000
            } else {
                port -= 2000
            }
        }
        return port.toString()
    }

    // LocalDateTime seems to be the only type that is supported across all drivers atm
    // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
    @Bean
    fun r2dbcCustomConversions(connectionFactory: ConnectionFactory): R2dbcCustomConversions {
        val dialect = DialectResolver.getDialect(connectionFactory)
        val converters = dialect.converters.toMutableList()
        converters.add(InstantWriteConverter())
        converters.add(InstantReadConverter())
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS)
        return R2dbcCustomConversions(CustomConversions.StoreConversions.of(dialect.simpleTypeHolder, converters), mutableListOf<Any>())
    }

    @WritingConverter
    class InstantWriteConverter : Converter<Instant, LocalDateTime> {
        override fun convert(source: Instant) = LocalDateTime.ofInstant(source, ZoneOffset.UTC)
    }

    @ReadingConverter
    class InstantReadConverter : Converter<LocalDateTime, Instant> {
        override fun convert(localDateTime: LocalDateTime) = localDateTime.toInstant(ZoneOffset.UTC)
    }

    @Bean
    fun db(mapDbProperties: MapDbProperties): DB {
        return DBMaker.fileDB(mapDbProperties.fileResource.file).make()
    }
}
