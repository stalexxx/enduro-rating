package com.example.guestbook

import com.example.guestbook.Rand.randName
import com.example.guestbook.Rand.randNumber
import io.requery.kotlin.`in`
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import java.sql.Timestamp
import java.time.LocalDateTime.*
import kotlin.reflect.full.createInstance

class RatingRepository(private var configuration: KotlinConfiguration, private var data: KotlinEntityDataStore<Any>) {
    var algos = Alogos()

    fun resetRating(season: Season) {
        data.invoke {
            select(Racer::class).get().forEach { rcr ->
                insert(RatingEntity().apply {
                    racer = rcr
                    createdAt = Timestamp(System.currentTimeMillis())
                    rating = 1000.0
                    this.season = season
                })
            }
        }
    }

    fun resetRating(racer: Racer) {
        data.invoke {
            insert(RatingEntity().apply {
                this.racer = racer
                createdAt = Timestamp(System.currentTimeMillis())
                rating = 1000.0
            })
        }
    }

    fun handleResultSet(results: List<ResultEntity>) {
        isSingleEvent(results)

        val rating = data.select(RatingEntity::class).get().map { it.racer.id to it.rating }.toMap()

        results.map { result ->
            result.rateDelta = algos.delta(result, results.filter { it != result }, rating)
            result
        }.also { res ->
            data.insert(entities = res)
            updateRatings(results.map { it.racer to it.rateDelta }.toMap())
        }

    }

    private fun updateRatings(map: Map<Racer, Double>) {
        data.invoke {
            (select(Rating::class) where (Rating::racer `in` map.keys)).get().forEach {
                val delta = map[it.racer]
                if (delta != null) {
                    it.rating += delta
                }
                update(it)
            }
        }
    }

    class Alogos {
        fun delta(current: Result, others: List<Result>, rating: Map<Int, Double>): Double {
            val (higher, lower) = others.partition { it.position < current.position }

            return lower.fold(0.0) { acc: Double, oponent: Result ->
                acc + lowerCoeff(current, oponent, rating)
            } - higher.fold(0.0) { acc: Double, oponent: Result ->
                acc + higherCoeff(current, oponent, rating)
            }
        }

        private fun higherCoeff(current: Result, oponent: Result, rating: Map<Int, Double>): Double {
            val currentRating = rating[current.racer.id]!!
            val opponentRating = rating[oponent.racer.id]!!
            return when {
                currentRating > opponentRating -> 15.0
                currentRating < opponentRating -> 5.0
                else -> 10.0
            }
        }

        private fun lowerCoeff(current: Result, oponent: Result, rating: Map<Int, Double>): Double {
            val currentRating = rating[current.racer.id]!!
            val opponentRating = rating[oponent.racer.id]!!
            return when {
                currentRating > opponentRating -> 5.0
                currentRating < opponentRating -> 15.0
                else -> 10.0
            }
        }
    }

    fun addSeason(apply: Season): Season = data.insert(apply)

    fun addResult(apply: Result): Result = data.insert(apply)

    fun addEvent(event: Event): Event = data.insert(event)

    fun addRating(rating: Rating): Rating = data.insert(rating)

    fun addRacer(racer: Racer): Racer = data.insert(racer)

    fun getRacers() = data.select(Racer::class).get().toList()
}


class TestDataController(private var repository: RatingRepository) {

    init {
        generateResults(
                generateRacers(7),
                generateEvents(5))
    }

    private fun generateResults(racers: List<Racer>, events: List<Event>) {
        repository.resetRating(events.first().season)
        events.forEach { evt ->
            var plc = 1
            Rand.randomSublist(racers).map { rc ->
                ResultEntity().apply {
                    racer = rc
                    event = evt
                    position = plc++
                    result = Rand.enum(FinalResult.values())
                    regNumber = racer.number

                }
            }.also {



                repository.handleResultSet(it.toList())
            }
        }
    }

    private fun generateEvents(count: Int) : List<Event> =
            SeasonEntity().apply {
                startDate = Rand.localDate(365)
                endDate = Rand.localDate(1)
                description = Rand.randName(3)
            }.let {
                repository.addSeason(it)
            }.let {
                return generate<EventEntity>(count) {
                    title = Rand.randName(2)
                    startDate = Rand.localDate(365)
                    season = it
                }.map {
                    repository.addEvent(it)
                }.toList()
            }


    private fun generateRacers(count: Int) =
            generate<RacerEntity>(count) {
                name = randName(3)
                number = randNumber(999)
                created_at = now()
            }.map {
                repository.addRacer(it)
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

