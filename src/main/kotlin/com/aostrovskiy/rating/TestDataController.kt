package com.aostrovskiy.rating

import com.aostrovskiy.rating.Rand.randName
import com.aostrovskiy.rating.Rand.randNumber
import java.time.LocalDateTime.now
import kotlin.reflect.full.createInstance


class TestDataController(private var service: RatingService) {

    init {
        generateResults(
                generateRacers(3),
                generateEvents(2))
    }

    private fun generateResults(racers: List<Racer>, events: List<Event>) {
        service.resetRating(events.first().season)
        events.forEach { evt ->
            var plc = 1
            Rand.randomSublist(racers).map { rc ->
                com.aostrovskiy.rating.ResultEntity().apply {
                    racer = rc
                    event = evt
                    position = plc++
                    result = Rand.enum(FinalResult.values())
                    regNumber = racer.number

                }
            }.also {



                service.handleResultSet(it.toList())
            }
        }
    }

    private fun generateEvents(count: Int) : List<Event> =
            com.aostrovskiy.rating.SeasonEntity().apply {
                startDate = Rand.localDate(365)
                endDate = Rand.localDate(1)
                description = Rand.randName(3)
            }.let {
                service.addSeason(it)
            }.let {
                return generate<EventEntity>(count) {
                    title = randName(2)
                    startDate = Rand.localDate(365)
                    season = it
                }.map {
                    service.addEvent(it)
                }.toList()
            }


    private fun generateRacers(count: Int) =
            generate<RacerEntity>(count) {
                name = randName(3)
                number = randNumber(999)
                created_at = now()
            }.map {
                service.addRacer(it)
            }.toList()
}

inline fun <reified T : Any> generate(count: Int, crossinline reciever: T.() -> Unit): Sequence<T> {
    return generateSequence {
        T::class.createInstance().apply(reciever)
    }.take(count)
}


//asserting extensions
internal fun isSingleEvent(results: List<ResultEntity>) {
    assert(results.map { it.event }.toSet().size == 1) { "more than one event" }
}


//

