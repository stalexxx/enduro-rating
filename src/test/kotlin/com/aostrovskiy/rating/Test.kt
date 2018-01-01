package com.aostrovskiy.rating

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class Test : StringSpec() {



    init {
        "sizes" {

            val ds = BaseConfig.config.ds
            val racers = ds.select(Racer::class).get().collect(mutableListOf())
            val events = ds.select(Event::class).get().collect(mutableListOf())

            racers.size shouldBe 3
            events.size shouldBe 2
        }
    }
}