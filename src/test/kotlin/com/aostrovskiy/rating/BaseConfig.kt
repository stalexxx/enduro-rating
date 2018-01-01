package com.aostrovskiy.rating

import io.kotlintest.ProjectConfig
import io.kotlintest.ProjectExtension
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import org.h2.jdbcx.JdbcConnectionPool
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

object BaseConfig : ProjectConfig() {

    val config = Config(h2Connnection(), false)

    private fun h2Connnection(): DataSource {
        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
            user = "h2"
            password = ""
        }
        return JdbcConnectionPool.create(dataSource)
    }

    override val extensions: List<ProjectExtension>
        get() = listOf(GenerateExtension)

}

object GenerateExtension : ProjectExtension {


    operator fun invoke(init: TestDataGenerator.() -> Unit) : KotlinEntityDataStore<Any> {
        generator.reset()
        generator.init()

        return BaseConfig.config.ds
    }

    val generator: TestDataGenerator =
            RatingService(configuration = BaseConfig.config.configuration, data = BaseConfig.config.ds).let { TestDataGenerator(it) }

    override fun beforeAll() {
        SchemaModifier(BaseConfig.config.configuration).createTables(TableCreationMode.DROP_CREATE)
    }

    override fun afterAll() {
        println("after all extension")
    }
}