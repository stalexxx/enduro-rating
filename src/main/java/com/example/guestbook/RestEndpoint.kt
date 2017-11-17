package com.example.guestbook

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectWriter
import com.google.appengine.api.ThreadManager
import com.google.appengine.repackaged.com.google.gson.GsonBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.requery.cache.EntityCacheBuilder
import io.requery.jackson.EntityMapper
import io.requery.kotlin.desc
import io.requery.kotlin.eq
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import org.ehcache.jsr107.EhcacheCachingProvider
import org.postgresql.ds.PGPooledConnection
import org.postgresql.ds.PGPoolingDataSource
import org.postgresql.ds.PGSimpleDataSource
import spark.Request
import spark.Spark
import spark.servlet.SparkApplication
import java.sql.DriverManager
import javax.sql.DataSource


class RestEndpoint : SparkApplication {

    var ds: KotlinEntityDataStore<Any>
    var configuration: KotlinConfiguration
    var entityMapper: EntityMapper

    init {
        val (configuration, data) = data()
        ds = data
        this.configuration = configuration
        entityMapper = EntityMapper(Models.DEFAULT, ds.data)

    }


    override fun init() {
        val mapper = entityMapper
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)

        val writer = mapper.writer()

        Spark.port(8080)
        Spark.get("/") { _, _ -> "ping".toJson(writer) }

        Spark.get("/event/:id") { req, res ->
            ds.select(Event::class)
                    .where(Event::id eq req.id())
                    .get()
                    .first()
                    .toJson(writer)
        }

        Spark.get("/racer/:id") { req, res ->
            ds.select(Racer::class)
                    .where(Event::id eq req.id())
                    .get()
                    .first()
                    .toJson(writer)

        }

        Spark.get("/result/:id") { req, res ->
            ds.invoke {

                select(Result::class)
                        .where(Result::id eq req.id())
                        .get()
                        .first()
                        .toJson(writer)

            }
        }

        Spark.get("/rating") { req, res ->
            ds.select(Racer::class)
                    .get()

            ds.select(Rating::class).
                    orderBy(Rating::rating.desc())
                    .get()
                    .toList()
                    .toJson(writer)
        }

        Spark.get("/init_db") { req, res ->
            SchemaModifier(configuration).createTables(TableCreationMode.DROP_CREATE)
            "done".toJson(writer)
        }

        Spark.get("/init_data") { req, res ->
            TestDataController(RatingRepository(configuration, ds))
            "done".toJson(writer)
        }


    }
}


fun main(args: Array<String>) {
    val (configuration, data) = data()

//    SchemaModifier(configuration).createTables(TableCreationMode.DROP_CREATE)
//    TestDataController(RatingRepository(configuration, data))

    val entityMapper = EntityMapper(Models.DEFAULT, data.data)

    data.invoke {


//
//        select(Racer::class)
//                .get().toList().forEach { println(it) }
//
//        select(Event::class)
//                .get().toList().forEach { println(it) }
//
//        select(Season::class)
//                .get().toList().forEach { println(it) }
//
//        select(Result::class)
//                .get().toList().forEach { println(it) }

//        val toList = select(Rating::class).
//                orderBy(Rating::rating.desc())
//                .get().toList()
//        print(toList)

        val mapper = entityMapper
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)

        val racerWriter = mapper.writerWithView(Views.Racer::class.java)
        val ratingWriter = mapper.writerWithView(Views.Rating::class.java)

        select(Racer::class).get().toList()

        val season = select(Season::class).where(Season::id eq 1).get().first()
        val toList = select(Rating::class).where(Rating::season eq season).get().toList()
        var toJson = toList.map { it.toJson(ratingWriter)}
        print(toJson)
        toJson = toList.map { it.toJson(ratingWriter)}
        print(toJson)


    }

}

private fun data(): Pair<KotlinConfiguration, KotlinEntityDataStore<Any>> {
    val ds = simpleRemoteDS()//localConnnection()
//    val ds = localConnnection()
    SchemaModifier(ds, Models.DEFAULT).createTables(TableCreationMode.CREATE_NOT_EXISTS)

    val configuration = KotlinConfiguration(dataSource = ds, model = Models.DEFAULT, useDefaultLogging = true
            ,
            cache = EntityCacheBuilder(Models.DEFAULT)
                    .useReferenceCache(true)
                    .useCacheManager(EhcacheCachingProvider().cacheManager)
                    .useSerializableCache(true)
                    .build()
    )

    val data = KotlinEntityDataStore<Any>(configuration)
    return Pair(configuration, data)
}

private fun localConnnection(): PGSimpleDataSource {
    return PGSimpleDataSource().apply {
        databaseName = "db1"
        this.user = "db1"
        this.password = "db1"
        serverName = "localhost"
        portNumber = 5432
    }
}

private fun remoteConnection(): DataSource =
        HikariDataSource(HikariConfig().apply {
            threadFactory = ThreadManager.backgroundThreadFactory()
            initializationFailTimeout = -1
            dataSource = simpleRemoteDS()
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

class ExPGDs: PGSimpleDataSource() {
    val pgPooledConnection by lazy {
        PGPooledConnection(DriverManager.getConnection(System.getProperty("cloudsql-local")), false)
    }
    override fun getConnection() = DriverManager.getConnection(System.getProperty("cloudsql-local")) //pgPooledConnection.connection
//        try {
//            val env = ApiProxy.getCurrentEnvironment()
//            val attr = env.attributes
//            val hostname = attr["com.google.appengine.runtime.default_version_hostname"] as String
//            println("hostname ")
//            System.getProperty("sqlURL")
//            val url = if (hostname.contains("localhost:"))
//                System.getProperty("sqlURL")
//            else
//                System.getProperty("cloudsql")
//            try {
//                return DriverManager.getConnection(url)
//            } catch (e: SQLException) {
//                throw ServletException("Unable to connect to Cloud SQL", e)
//            }
//
//        } finally {
//             Nothing really to do here.
//        }

}

fun init() {



}


fun Any.toJson(writer: ObjectWriter): String {
    return writer.writeValueAsString(this)
}


fun <T : Any> T.toJson(): String {


    val gson = GsonBuilder().run {
        disableHtmlEscaping()
        setPrettyPrinting()
        create()
    }
    val o = this
    return when (o) {
        is String -> o
        else -> gson.toJson(o)
    }
}

fun Request.id() = this.params("id")
