package com.anw.tenistats

class MatchView {
    var data: Long = 0
    var player1: String = ""
    var player2: String = ""
    var pkt1: String = ""
    var pkt2: String = ""
    var set1p1: String = ""
    var set1p2: String = ""
    var set2p1: String = ""
    var set2p2: String = ""
    var set3p1: String = ""
    var set3p2: String = ""
    var LastServePlayer: String = ""
    var winner: String =""


    // Konstruktor główny
    constructor()

    // Konstruktor, umożliwiający tworzenie obiektu bez podawania wszystkich parametrów
    constructor(data: Long, player1: String, player2: String, pkt1: String, pkt2: String, set1p1: String, set1p2: String, set2p1: String, set2p2: String,set3p1: String, set3p2: String, LastServePlayer: String, winner: String) {
        this.data = data
        this.player1 = player1
        this.player2 = player2
        this.pkt1= pkt1
        this.pkt2= pkt2
        this.set1p1= set1p1
        this.set1p2= set1p2
        this.set2p1= set2p1
        this.set2p2= set2p2
        this.set3p1= set3p1
        this.set3p2= set3p2
        this.LastServePlayer= LastServePlayer
        this.winner= winner
    }

    // Metoda do konwersji daty w formacie long na obiekt typu Date
    fun getDateAsMilliseconds(): Long {
        return data
    }
}




