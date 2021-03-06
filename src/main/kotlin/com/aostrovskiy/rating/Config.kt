package com.aostrovskiy.rating

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectWriter
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.requery.cache.EntityCacheBuilder
import io.requery.jackson.EntityMapper
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import org.postgresql.ds.PGPoolingDataSource
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

class Config(
        val dataSource: DataSource = remotePooledConnection(herokuConnnection()),
        val logging: Boolean = true
) {
    val configuration: KotlinConfiguration
        get() = KotlinConfiguration(
                dataSource = dataSource,
                model = Models.DEFAULT,
                useDefaultLogging = logging,
                cache = EntityCacheBuilder(Models.DEFAULT)
                        .useReferenceCache(logging)
                        .build()
        )

    val ds: KotlinEntityDataStore<Any>
        get() = KotlinEntityDataStore(configuration)

    val entityMapper: EntityMapper
        get() = EntityMapper(Models.DEFAULT, ds.data)

    fun writer(): ObjectWriter {
        val mapper = entityMapper
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        return mapper.writer()
    }
}

fun localConnnection(): PGSimpleDataSource {
    return PGSimpleDataSource().apply {
        databaseName = "postgres"
        this.user = "alex"
        this.password = ""
        serverName = "localhost"
        portNumber = 5432
    }
}

private fun herokuConnnection(): PGSimpleDataSource {
    return PGSimpleDataSource().apply {
        databaseName = "d5g9t5l9mkp17o"
        this.user = "sdzwnnnasglvqx"
        this.password = "af6833687a169ec83e164bde89b67f29a2b079dabcc2c8a0683bfb765cf1bc95"
        serverName = "ec2-174-129-195-73.compute-1.amazonaws.com"
        portNumber = 5432
        ssl = true
        sslfactory = "org.postgresql.ssl.NonValidatingFactory"
    }
}

private fun remotePooledConnection(dataSource: DataSource): DataSource =
        HikariDataSource(HikariConfig().apply {
            //            threadFactory = ThreadManager.backgroundThreadFactory()
            initializationFailTimeout = -1
            this.dataSource = dataSource
        }).apply {

        }

private fun simpleRemoteDS(): DataSource {
    return PGPoolingDataSource().apply {
        databaseName = "enduro"
        user = "enduro"
        password = "enduro"
        serverName = "google"
        socketFactory = "com.google.cloud.sql.postgres.SocketFactory"
        socketFactoryArg = "enduro-184119:us-central1:pgenduro"

//        setProperty("cloudSqlInstance", "enduro-184119:us-central1:pgenduro")
//        setProperty("useSSL", "false")


//        portNumber = 5432
    }
}
