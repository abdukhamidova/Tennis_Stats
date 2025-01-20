package com.anw.tenistats

data class EventDataClass(
    var id: String? = null,
    var name: String? = null,
    var startDate: Long? = 0L,
    var endDate: Long? = 0L,
    var note: String? = null,
    var players: List<String> = emptyList(),
    var isCoachChecked: Boolean? = false
)