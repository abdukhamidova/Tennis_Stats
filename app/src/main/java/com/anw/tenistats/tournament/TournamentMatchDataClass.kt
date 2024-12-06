package com.anw.tenistats.tournament

data class TournamentMatchDataClass(
    var number: String,
    var matchId: String,
    var player1: String,
    var player2: String,
    var winner: String,
    val set1p1: String,
    val set2p1: String,
    val set3p1: String,
    val set1p2: String,
    val set2p2: String,
    val set3p2: String,
    val changes: Boolean = false
)



data class Round(
    val match1: TournamentMatchDataClass,
    val match2: TournamentMatchDataClass
)
