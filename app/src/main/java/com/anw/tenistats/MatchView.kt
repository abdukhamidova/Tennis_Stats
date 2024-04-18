package com.anw.tenistats

import java.util.Calendar

class MatchView() {
    var data: Long = 0
    var player1: String = ""
    var player2: String = ""

    constructor(data: Long, player1: String, player2: String) : this() {
        this.data = data
        this.player1 = player1
        this.player2 = player2
    }
}
