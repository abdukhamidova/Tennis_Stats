package com.anw.tenistats

//import java.sql.Date

/*data class Uzytkownik(
    val matches: MutableList<Match> = mutableListOf(), //lista meczow danego uzytkownika
    val players: MutableList<Player> = mutableListOf() //lista playerow danego uzytkownika
)

data class Match(
    val id: String, //identyfikator z bazy
    val date: Date, //format data -> SYSDATE
    val player1: Player, //pierwszy grajacy player
    val player2: Player, //drugi grajacy player
    val sets: MutableList<Set> = mutableListOf() //lista poszczegolnych setow po kolei
)*/

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
    var active: Boolean? = null
)

{
    //constructor() : this(null, null, null, null, null, null, null, null, null)
    constructor() : this(null, null, null, null, null, null, null, null, null, null)
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
