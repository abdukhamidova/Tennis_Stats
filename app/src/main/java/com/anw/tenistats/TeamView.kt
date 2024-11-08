package com.anw.tenistats

class TeamView {
    var name: String = ""
    var numberOfPeople: Int = 0
    var players: ArrayList<String> = ArrayList()  // Lista zawodników

    constructor()

    constructor(name: String, numberOfPeople: Int, players: ArrayList<String>) {
        this.name = name
        this.numberOfPeople = numberOfPeople
        this.players = players
    }
}