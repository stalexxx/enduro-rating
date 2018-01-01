package com.aostrovskiy.rating

//import com.google.appengine.api.ThreadManager
//import org.ehcache.jsr107.EhcacheCachingProvider
import com.fasterxml.jackson.databind.ObjectWriter
import io.requery.jackson.EntityMapper
import io.requery.kotlin.desc
import io.requery.kotlin.eq
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import org.postgresql.ds.PGPooledConnection
import org.postgresql.ds.PGSimpleDataSource
import spark.Request
import spark.Spark
import spark.servlet.SparkApplication
import java.sql.DriverManager

class RestEndpoint : SparkApplication {
    val config : Config = Config()
    val ds: KotlinEntityDataStore<Any>
        get() = config.ds



    override fun init() {
        val writer = config.writer()

//        val racerWriter = mapper.writerWithView(Views.Racer::class.java)
        val ratingWriter = config.entityMapper.writerWithView(Views.Rating::class.java)


        Spark.port(8080)
        Spark.get("/") { _, _ -> "ping" }

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
                    .toJson(ratingWriter)
        }

        Spark.get("/init_db") { req, res ->
            SchemaModifier(config.configuration).createTables(TableCreationMode.DROP_CREATE)
            "done".toJson(writer)
        }

        Spark.get("/init_data") { req, res ->
            TestDataController(RatingRepository(config.configuration, ds))
            "done".toJson(writer)
        }


    }
}


fun main(args: Array<String>) {
    val (configuration, data) = data(localConnnection())

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
//        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)

        val racerWriter = mapper.writerWithView(Views.Racer::class.java)
        val ratingWriter = mapper.writer()
//        val ratingWriter = mapper.writerWithView(Views.Rating::class.java)

//        select(Racer::class).get().toList()

        val season = select(Season::class).where(Season::id eq 1).get().first()
        val toList = select(Rating::class).where(Rating::season eq season).get().toList()
        var toJson = toList.map { it.toJson(ratingWriter) }
        print(toJson)

    }

}


class ExPGDs : PGSimpleDataSource() {
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


fun Request.id() = this.params("id")
