package com.anw.tenistats.stats

//import java.sql.Date

/*data class Uzytkownik(
    val matches: MutableList<Match> = mutableListOf(), //lista meczow danego uzytkownika
    val players: MutableList<Player> = mutableListOf() //lista playerow danego uzytkownika
)*/

//do Head2Head
data class Match(
    val id: String = "",
    val player1: String = "",
    val player2: String = "",
    val data: Long = 0L,
    val set1p1: String = "",
    val set1p2: String = "",
    val set2p1: String = "",
    val set2p2: String = "",
    val set3p1: String = "",
    val set3p2: String = "",
    val pkt1: String = "",
    val pkt2: String = "",
    val winner: String? = null
)

data class Player(
    var firstName: String? = null,
    var lastName: String? = null,
    var player: String? = null,
    var duplicate: Int? = null,
    var nationality: String? = null,
    var dateOfBirth: Long? = null,
    var handedness: String? = null,
    var strength: String? = null,
    var weakness: String? = null,
    var active: Boolean? = null,
    var note: String? = null,
    val team: List<String> = emptyList(),
    var isFavorite: Boolean = false
)

{
    //constructor() : this(null, null, null, null, null, null, null, null, null)
    //constructor() : this(null, null, null, null, null, null, null, null, null, null,null,null,false)
}

/*data class Set(
    val id: Int, //po kolei set 1, 2, 3
    val gems: MutableList<Game> = mutableListOf() //lista gemow w danym secie
)

data class Game(
    val set1: Int, //ilosc gemow playera 1 w danym secie
    val set2: Int, //ilosc gemow playera 2 w danym secie
    val points: MutableList<Point> = mutableListOf() //lista punktow w danym gemie
)*/

data class Point(
    val pkt1: String, //ilosc punktow playera 1 w danym gemie
    val pkt2: String, //ilosc punktow playera 2 w danym gemie
    val kto: String, //player, ktory cos zagral
    val co: String, //np. ace, winner, forced error itp
    val gdzie: String, //np. return, ground, slice itp
    val czym: String, //FH/BH
    val serwis: Int, //1 - wszedl pierwszy serwis, 2 - wszedl drugi serwis, 0 - podwojny blad
    val servePlayer: String //osoba serwujaca
)
