package com.anw.tenistats

class MatchView {
    var data: Long = 0
    var player1: String = ""
    var player2: String = ""

    // Konstruktor główny
    constructor()

    // Konstruktor, umożliwiający tworzenie obiektu bez podawania wszystkich parametrów
    constructor(data: Long, player1: String, player2: String) {
        this.data = data
        this.player1 = player1
        this.player2 = player2
    }

    // Metoda do konwersji daty w formacie long na obiekt typu Date
    fun getDateAsMilliseconds(): Long {
        return data
    }
}




