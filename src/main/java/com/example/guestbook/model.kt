package com.example.guestbook

import io.requery.*
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.JoinColumn

@io.requery.Table(name = "events")
@Entity
interface Event1 : Persistable {

    @get:Key
    @get:Generated
    var id: Int

    var title: String

    var startDate: LocalDate

    @get:OneToMany
    var results: List<Result1>
}

@io.requery.Table(name = "racers")
@Entity
interface Racer1 : Persistable {
    @get:Key
    @get:Generated
    var id: Int

    var name: String

    var number: Int

    var created_at: LocalDateTime

    @get:OneToMany
    var results: List<Result1>
}


@io.requery.Table(name = "results")
@Entity
interface Result1 : Persistable {
    @get:Key
    @get:Generated
    var id: Int

    @get:ManyToOne
    @get:Column(name = "event_id")
    var event: Event1

    @get:ManyToOne
    @get:Column(name = "racer_id")
    var racer: Racer1

    var place: Int

}

@io.requery.Table(name = "ratings")
@Entity
interface Rating : Persistable {

    @get:OneToOne
    @get:JoinColumn(name = "result_id")
    var results: Result1

    var rating: Int

    var created_at: Timestamp
}