package com.aostrovskiy.rating

import io.kotlintest.TestCaseContext
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec


val resetDb: (TestCaseContext, () -> Unit) -> Unit = { context, testCase ->

    //    val generator = BaseConfig.generator
//
//    generator.generateRacers(20)
//    generator.generateEvents(1)

    testCase()
}


class SimpleTest : StringSpec() {

    init {
        "size racers" {
            forAll(Gen.choose(1, 2)) { count: Int ->
                GenerateExtension {
                    generateRacers(count)
                }.invoke {
                    select(Racer::class).get().toList().size == count
                }
            }
        }

        "size events" {
            forAll(Gen.choose(1, 2)) { count: Int ->
                GenerateExtension {
                    generateEvents(count)
                }.invoke {
                    select(Event::class).get().toList().size == count
                }
            }
        }
    }
}

class TestResults : StringSpec() {
    init {
        "results" {
            GenerateExtension {
                val racers = generateRacers(10)
                val events = generateEvents(1)
                generateResults(racers, events)
            }.invoke {
                select(Result::class).get().toList().size shouldBe 1
            }
        }

    }
}