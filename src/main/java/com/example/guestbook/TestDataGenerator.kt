package com.example.guestbook

import io.requery.kotlin.eq
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import java.time.LocalDateTime

class TestDataGenerator(configuration: KotlinConfiguration, private var data: KotlinEntityDataStore<Any>) {
    init {
        val events = generateEvents(10)
        generateResults(generateRacers(20), events)

        data.invoke {
            val offset = select(Event1::class) where (Event1::id eq events.first().id) limit 5
            val get = offset.get().first()
            val results = get.results
            println(results)
        }
    }

    private fun generateResults(racers: List<Racer1>, events: List<Event1>) {
        events.flatMap { evt ->
            var plc = 1
            Rand.randomSublist(racers).map { rc->
                Result1Entity().apply {
                    racer = rc
                    event = evt
                    place = plc++
                }
            }
        }.forEach { data.insert(it) }
    }

    private fun generateEvents(count: Int): List<Event1> {
        return generateSequence {
            Event1Entity().apply {
                title = Rand.randName(2)
                startDate = Rand.localDate(365)
            }
        }.take(count).map {
            data.insert(it)
        }.toList()
    }

    private fun generateRacers(count: Int): List<Racer1> {
        return generateSequence {
            Racer1Entity().apply {
                name = Rand.randName(3)
                number = Rand.randNumber(999)
                created_at = LocalDateTime.now()
            }
        }.take(count).map {
            data.insert(it)
        }.toList()
    }


}