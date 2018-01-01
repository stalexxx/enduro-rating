package com.aostrovskiy.rating

import io.kotlintest.ProjectConfig
import io.kotlintest.ProjectExtension
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import org.h2.jdbcx.JdbcConnectionPool
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

object BaseConfig : ProjectConfig(){

    val config = Config(h2Connnection(), false)

    private fun h2Connnection(): DataSource {

        val dataSource = JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
        dataSource.user = "sa"
        dataSource.password = "sa"
        return JdbcConnectionPool.create(dataSource)
    }

    override val extensions: List<ProjectExtension>
        get() = listOf(TestExtension)

    override fun beforeAll() {

        RatingService(configuration = config.configuration, data = config.ds).also {
            SchemaModifier(config.configuration).createTables(TableCreationMode.DROP_CREATE)
            TestDataController(it)
        }

    }
}

object TestExtension : ProjectExtension {
    override fun beforeAll() {
        println("before all extension")
    }

    override fun afterAll() {
        println("after all extension")
    }
}