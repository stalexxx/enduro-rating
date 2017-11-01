package com.example.guestbook

import com.example.guestbook.tables.*
import com.google.appengine.repackaged.com.google.gson.GsonBuilder
import io.requery.kotlin.eq
import io.requery.sql.*
import kdbc.KDBC
import org.postgresql.ds.PGSimpleDataSource
import spark.Request
import spark.Response
import spark.Spark
import spark.servlet.SparkApplication
import java.sql.Connection
import java.sql.DriverManager
import java.time.*
import java.util.*

private fun initPG() {
    KDBC.setDataSource(PGSimpleDataSource().apply {
        databaseName = "enduro"
        user = "enduro"
        password = "enduro"
        serverName = "localhost"
        portNumber = 5432
    })
    KDBC.debug = true

}

private fun initPG_(dbName: String = "db1", user: String = "db1", pw: String = "db1") {
    KDBC.setDataSource(PGSimpleDataSource().apply {
        databaseName = dbName
        this.user = user
        this.password = dbName
        serverName = "localhost"
        portNumber = 5432
    })
    KDBC.debug = true

}

class RestEndpoint : SparkApplication {

    init {
        initPG()

    }


    override fun init() {
        Spark.port(8080)
        Spark.get("/ping", ::ping)
        Spark.get("/event/:id") { req, res ->
            SelectEvent().byId(req.params("id").toInt())?.toJson() ?: "not found"
        }

        Spark.get("/racer/:id") { req, res ->
            SelectRacer().byId(req.params("id").toInt())?.toJson() ?: "not found"
        }

        Spark.get("/result/:id") { req, res ->
            SelectResult().byId(req.params("id").toInt())?.toJson() ?: "not found"
        }
    }
}

fun main(args: Array<String>) {
    val ds = PGSimpleDataSource().apply {
        databaseName = "db1"
        this.user = "db1"
        this.password = "db1"
        serverName = "localhost"
        portNumber = 5432
    }
    SchemaModifier(ds, Models.DEFAULT).createTables(TableCreationMode.CREATE_NOT_EXISTS)

    val configuration = KotlinConfiguration(dataSource = ds, model = Models.DEFAULT, useDefaultLogging = true)
    val data = KotlinEntityDataStore<Any>(configuration)

    TestDataGenerator(configuration, data)

//    data.invoke {
//
//        //        val event1Entity = insert(Event1Entity().apply { title = "lalala" })
////        val racer1Entity = insert(Racer1Entity().apply { name = "Ivan Ivanov" })
//
//
//        val insert = insert(Result1Entity().apply {
//            place = 1
//            racer = Racer1Entity().apply {
//                name = "Ivan Ivanov"
//                number = 41
//                created_at = LocalDateTime.now()
//            }
//            event = Event1Entity().apply {
//                title = "lalala"
//                startDate = LocalDate.now()
//            }
//        })
//
//        val result = select(Event1::class) where (Event1::id eq 1) limit 5
//
//
//        val entityDataStore = EntityDataStore<Any>(configuration)
//        var mapper = EntityMapper(Models.DEFAULT, entityDataStore)
//        val writer = mapper.writer()
//
//
//
//        result.get().forEach {
//            print(writer.writeValueAsString(it))
//        }
//
//    }
}







fun ping(req: Request, res: Response): Any {
    val connection = connection()

    //[END doc-example]

    connection.createStatement().use { statement ->
        val resultSet = statement.executeQuery(
                "SELECT schemaname, tablename FROM pg_catalog.pg_tables")
        while (resultSet.next()) {
            println(resultSet.getString(1) + "." + resultSet.getString(2))
        }

    }
    Connection1().check()



    return "ping".toJson()
}


private fun connection(): Connection {
    val instanceConnectionName = "enduro-184119:us-central1:pgenduro"
    val databaseName = "enduro"
    val username = "enduro"
    val password = "enduro"
//        val jdbcUrl =
//                "jdbc:postgresql://google/$databaseName?socketFactory=com.google.cloud.sql.postgres.SocketFactory" + "&socketFactoryArg=$instanceConnectionName"

    return DriverManager.getConnection("jdbc:postgresql://localhost:5432/enduro", username, password)
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