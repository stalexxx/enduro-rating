package com.aostrovskiy.rating

import io.requery.kotlin.`in`
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import java.sql.Timestamp

class RatingRepository(private var configuration: KotlinConfiguration, private var data: KotlinEntityDataStore<Any>) {
    var algos = Alogos()

    fun resetRating(season: Season) {
        data.invoke {
            select(Racer::class).get().forEach { rcr ->
                insert(com.aostrovskiy.rating.RatingEntity().apply {
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
            insert(com.aostrovskiy.rating.RatingEntity().apply {
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