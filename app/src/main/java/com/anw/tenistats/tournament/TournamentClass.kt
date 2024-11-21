package com.anw.tenistats.tournament

class TournamentClass {
    var id: String = ""
    var name: String=""
    var place: String=""
    var country: String =""
    var startDateStr: String = ""
    var endDateStr: String = ""
    var surface : String=""
    var note: String=""
    var creator: String=""

    // Public properties with custom logic
    val startDate: Long
        get() = startDateStr.toLongOrNull() ?: 0L

    val endDate: Long
        get() = endDateStr.toLongOrNull() ?: 0L

    constructor()

    constructor(name: String, place: String, country: String, start_date: Long, end_date: Long, surface: String, note: String, creator: String) {
        this.name = name
        this.place = place
        this.country = country
        this.startDateStr = startDate.toString()
        this.endDateStr = endDate.toString()
        this.surface = surface
        this.note = note
        this.creator = creator
    }
}