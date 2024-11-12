package com.anw.tenistats.player

class TeamView {
    var name: String = ""
    var players: ArrayList<String> = ArrayList()  // Lista zawodników

    constructor()

    constructor(name: String,players: ArrayList<String>) {
        this.name = name
        this.players = players
    }
}