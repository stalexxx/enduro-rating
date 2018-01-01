package com.aostrovskiy.rating

import io.kotlintest.ProjectConfig

class BaseConfig : ProjectConfig(){
    val config = Config()

    override fun beforeAll() {
        val repo = RatingRepository(configuration = config.configuration, data = config.ds)

    }
}