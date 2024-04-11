package com.anw.tenistats


data class Match(
    val row: MutableList<Point> = mutableListOf() //lista poszczegolnych punktow po kolei
)

data class Point(
    val set1p1: Int, //ilosc gemow playera 1 w secie 1
    val set1p2: Int, //ilosc gemow playera 2 w secie 1
    val set2p1: Int, //ilosc gemow playera 1 w secie 2
    val set2p2: Int, //ilosc gemow playera 2 w secie 2
    val set3p1: Int, //ilosc gemow playera 1 w secie 3
    val set3p2: Int, //ilosc gemow playera 2 w secie 3
    val pkt1: Int, //ilosc punktow playera 1 w danym gemie
    val pkt2: Int, //ilosc punktow playera 2 w danym gemie
    val kto: String, //player, ktory cos zagral
    val co: String, //np. ace, winner, forced error itp
    val gdzie: String, //np. return, ground, slice itp
    val czym: String, //FH/BH
    val serwis: Int //1 - wszedl pierwszy serwis 2 - wszedl drugi serwis 0 - podwojny blad
)
