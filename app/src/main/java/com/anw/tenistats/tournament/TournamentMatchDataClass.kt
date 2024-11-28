package com.anw.tenistats.tournament

data class TournamentMatchDataClass(
    var number: String,
    var matchId: String,
    var player1Name: String,
    var player2Name: String,
    var winner: String,
    val player1Set1: String,
    val player1Set2: String,
    val player1Set3: String,
    val player2Set1: String,
    val player2Set2: String,
    val player2Set3: String
)

data class Round(
    val match1: TournamentMatchDataClass,
    val match2: TournamentMatchDataClass
)
