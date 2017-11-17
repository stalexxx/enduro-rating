//package com.example.guestbook
//
//import io.requery.Generated
//import io.requery.Key
//import kdbc.Insert
//import kdbc.Query
//import kdbc.Table
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.util.*
//
//
//class EVENTS : Table() {
//    val id = column<Int>("id", "serial not null primary key ")
//    val title = column<String>("title", "text")
//    val date = column<Date>("date", "timestamp with time zone")
//}
//
//class RESULTS : Table() {
//    val id = column<Int>("id", "serial not null primary key")
//    val event = column<Int>("event_id", "integer REFERENCES events (id)")
//    val position = column<Int>("position", "integer not null")
//
//}
//
//class RACERS : Table() {
//    val id = column<Int>("id", "serial not null primary key ")
//    val name = column<String>("name", "text")
//    val number = column<Int>("number", "integer")
//    val created = column<Date>("created_at", "timestamp with time zone")
//}
//
//data class Event(
//        @get:Key
//        @get:Generated
//        var id: Int? = null,
//
//        var title: String,
//        var date: Date
//) {
//    constructor(t: EVENTS) : this(t.id(), t.title()!!, t.date()!!)
//}
//
//data class Racer(
//        var id: Int? = null,
//        var name: String,
//        var number: Int?,
//        var created: Date
//) {
//    constructor(t: RACERS) : this(t.id(), t.name()!!, t.number(), t.created()!!)
//}
//
//
//data class Result(
//        var id: Int? = null,
//        var event: Event,
//        var place: Int?
//) {
//    constructor(t: RESULTS, e: SelectEvent) : this(t.id(), e.byId(t.event()!!)!!, t.place()!!)
//}
//
//
//class SelectEvent : Query<Event>() {
//    val c = EVENTS()
//
//    init {
//        select(c)
//        from(c)
//    }
//
//    override fun get() = Event(c)
//
//    fun byId(id: Int) = firstOrNull {
//        where {
//            c.id `=` id
//        }
//    }
//}
//
//fun Event.insert() =
//        object : Insert() {
//            val c = EVENTS()
//
//            init {
//                insert(c) {
//                    c.title `=` this@insert.title
//                    c.date `=` LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
//                }
//                generatedKeys {
//                    this@insert.id = getInt(1)
//                }
//            }
//        }
//
//
//fun Result.insert() = object : Insert() {
//    val c = RESULTS()
//
//    init {
//        insert(c) {
//            c.event `=` this@insert.event.id
//            c.place `=` 1
////            c.place `=` LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
//
//        }
//        generatedKeys {
//            this@insert.id = getInt(1)
//        }
//    }
//}
//
//
//class InsertRacer(racer: Racer) : Query<Racer>() {
//    val c = RACERS()
//
//    init {
//        insert(c) {
//            c.name `=` racer.name
//            c.number `=` racer.number
//            c.created `=` LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
//        }
//        generatedKeys {
//            racer.id = getInt(1)
//        }
//
//    }
//
//}
//
//class SelectRacer : Query<Racer>() {
//    val c = RACERS()
//
//    init {
//        select(c)
//        from(c)
//    }
//
//    override fun get() = Racer(c)
//
//    fun byId(id: Int) = firstOrNull {
//        where {
//            c.id `=` id
//        }
//    }
//
//}
//
//class SelectResult : Query<Result>() {
//    val c = RESULTS()
//
//    init {
//        select(c)
//        from(c)
//    }
//
//    override fun get() = Result(c, SelectEvent())
//
//    fun byId(id: Int) = firstOrNull {
//        where {
//            c.id `=` id
//        }
//    }
//
//}
//
//private fun testKdbc() {
////    initPG_()
//    RACERS().create(skipIfExists = true, dropIfExists = false)
//    EVENTS().create(skipIfExists = true, dropIfExists = false)
//    RESULTS().create(skipIfExists = true, dropIfExists = false)
//
//    InsertRacer(Racer(name = "Ivan", number = 41, created = Date())).apply {
//        execute()
//    }
//
//    Event(title = "кабан", date = Date()).apply {
//        insert().execute()
//    }.let { event ->
//        Result(event = event, place = 1)
//    }.apply {
//        insert().execute()
//    }
//
////    Result(event = SeleE)
//
//
//    println(SelectRacer().list())
//    println(SelectEvent().list())
//    println(SelectResult().list())
//}