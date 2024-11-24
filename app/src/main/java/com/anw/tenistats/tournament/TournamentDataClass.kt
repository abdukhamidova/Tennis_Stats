package com.anw.tenistats.tournament

data class TournamentDataClass(
    var id: String? = null,
    var name: String? = null,
    var place: String? = null,
    var country: String? = null,
    var startDate: Long? = null,
    var endDate: Long? = null,
    var surface: String? = null,
    var note: String? = null,
    var creator: String? = null
)
