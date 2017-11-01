package com.example.guestbook

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*

object Rand {
    val nextDouble: Double
        get() = Random().nextDouble()

    val nextBool: Boolean
        get() = Random().nextBoolean()

    fun nextString(min: Int = 0, max: Int) = IntRange(0, int(min, max)).map { 'a' + randNumber(26) }.joinToString(separator = "")

    fun randName(i: Int): String =
            IntRange(0, i - 1).joinToString(" ") { nextString(5, 10) }

    fun randNumber(max: Int): Int = Math.round(max * nextDouble).toInt()

    fun int(min: Int, max: Int): Int = min + Random().nextInt((max - min) + 1)

    val nInstant: Instant
        get() {
            return Instant.now() - datetimeDuration()
        }

    fun localDate(max: Int) = (Instant.now() - dateDuration(max = max)).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun timeDuration(): Duration = Duration.ofHours(int(1, 23).toLong()) +
            Duration.ofMinutes(int(1, 59).toLong()) +
            Duration.ofSeconds(int(1, 59).toLong())


    private fun dateDuration(min: Int = 1, max: Int = 7): Duration = Duration.ofDays(int(min, max).toLong()) + timeDuration()

    private fun datetimeDuration(): Duration = dateDuration() + timeDuration()
    fun <T: Any> randomSublist(racers: List<T>): List<T> = racers.map { if (nextBool) it else null }. filter { it != null }.map {it!!}.toList()

}