package com.aostrovskiy.rating

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonView
import io.requery.*
import io.requery.converter.EnumStringConverter
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.JoinColumn

@io.requery.Table(name = "events")
@Entity
interface Event : Persistable {

    @get:Key
    @get:Generated
    var id: Int

    var title: String

    var startDate: LocalDate

    var country: String
    var city: String

    @get:OneToMany
    var results: List<Result>

    @get:ManyToOne
    @get:JsonManagedReference
    var season: Season
}

@io.requery.Table(name = "racers")
@Entity
interface Racer : Persistable {
    @get:Key
    @get:Generated
    @get:JsonView(Views.Rating::class)
    var id: Int

    @get:JsonView(Views.Rating::class)
    var name: String

    @get:JsonView(Views.Rating::class)
    var number: Int

    @get:JsonView(Views.Rating::class)
    var created_at: LocalDateTime

    @get:OneToMany
    @get:JsonBackReference
    var results: List<Result>
}

enum class FinalResult {OK, DISQUALIFICATION, INJURY}
class FinalResultConverter : EnumStringConverter<FinalResult>(FinalResult::class.java);

@io.requery.Table(name = "results")
@Entity
interface Result : Persistable {
    @get:Key
    @get:Generated
    var id: Int

    @get:ManyToOne
    @get:Column(name = "event_id")
    var event: Event

    @get:ManyToOne
    @get:Column(name = "racer_id")
    @get:JsonManagedReference
    var racer: Racer

    @get:Column(name = "reg_number")
    var regNumber:Int

    @get:Convert(FinalResultConverter::class)
    @get:Column(name = "final_result")
    var result: FinalResult

    var position: Int

    @get:Column(name = "rate_delta")
    var rateDelta: Double



}

@io.requery.Table(name = "ratings")
@Entity
interface Rating : Persistable {

    @get:Key
    @get:Generated
    @get:JsonView(Views.Rating::class)
    var id: Int

    @get:ManyToOne
//    @get:Column(name = "racer_id")
    @get:JoinColumn(name = "racer_id")
    @get:JsonView(Views.Rating::class)
    @get:JsonManagedReference
    var racer: Racer

    @get:OneToOne
    @get:JoinColumn(name = "result_id")
    var results: Result

    @get:JsonView(Views.Rating::class)
    var rating: Double

    @get:ManyToOne
    @get:Column(name = "season_id")
    var season: Season

    @get:Column(name = "created_at")
    var createdAt: Timestamp
}

@io.requery.Table(name = "seasons")
@Entity
interface Season: Persistable {
    @get:Key
    @get:Generated
    var id: Int

    @get:Column(name = "start_date")
    var startDate: LocalDate

    @get:Column(name = "end_date")
    var endDate: LocalDate

    var description: String

    @get:OneToMany
    @get:JsonBackReference
    var events: List<Event>
}


@io.requery.Table(name = "classes")
@Entity
interface RacerClass : Persistable {

}

class Views {
    class Racer
    class Rating


}