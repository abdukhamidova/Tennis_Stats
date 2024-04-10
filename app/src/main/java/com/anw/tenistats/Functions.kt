package com.anw.tenistats

import android.content.Intent
import android.widget.TextView
import android.content.Context

//poczatkowe ustawienie wyniku dla kazdej z aktywnosci z wynikiem
fun fillUpScoreInActivity(app: Stats,player1: TextView, player2: TextView, serve1: TextView, serve2: TextView, pkt1: TextView, pkt2: TextView, set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView): Unit {
    player1.text = app.player1
    player2.text = app.player2
    serve1.text = app.serve1
    serve2.text = app.serve2
    pkt1.text = app.pkt1
    pkt2.text = app.pkt2
    set1p1.text = app.set1p1
    set1p2.text = app.set1p2
    set2p1.text = app.set2p1
    set2p2.text = app.set2p2
    set3p1.text = app.set3p1
    set3p2.text = app.set3p2
}

//wyliczenie wyniku
fun score(context: Context,app: Stats,player1: TextView, player2: TextView, serve1: TextView, serve2: TextView, pkt1: TextView, pkt2: TextView, set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView) {
    if (app.czyTiebreak) { //gramy tiebreaka
        if (scoreTiebreak(pkt1, pkt2)) { //jesli prawda to koniec tiebreaka
            if (set2p1.text == "" && set2p2.text == "") { //jesli prawda to jestesmy w secie 1
                set1p1.text = "7"
                set2p1.text = "0"
                set2p2.text = "0"
            }
            else if(set3p1.text == "" && set3p2.text == ""){//jesli prawda to jestesmy w secie 2
                set2p1.text = "7"
                set3p1.text = "0"
                set3p2.text = "0"
            }
            else{
                set3p1.text = "7"
                fillUpScore(app,player1, player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                context.startActivity(Intent(context,EndOfMatchActivity::class.java)) //koniec meczu -> zmiana aktywnosci na EndOfMatch
            }
            pkt1.text = "0"
            pkt2.text = "0"
            if(serve1.text != ""){//zmiana serwujacego
                serve1.text = ""
                serve2.text = "1"
            }
            else{
                serve1.text = "1"
                serve2.text = ""
            }
            app.czyTiebreak=false
        }
    }
    else if (scorePkt(pkt1, pkt2)) { //jesli prawda to koniec gema
        if(serve1.text != ""){//zmiana serwujacego
            serve1.text = ""
            serve2.text = "1"
        }
        else{
            serve1.text = "1"
            serve2.text = ""
        }
        if (set2p1.text == "" && set2p2.text == "") { //jesli prawda to jestesmy w secie 1
            if (scoreSet(set1p1, set1p2)) { //jesli prawda to skonczyl sie 1 set
                set2p1.text = "0"
                set2p2.text = "0"
            }
            else {
                if (set1p1.text == "6" && set1p2.text == "6") { //jesli prawda to bedzie tiebreak
                    app.czyTiebreak = true
                }
            }
        }
        else if(set3p1.text == "" && set3p2.text == ""){ //jesli prawda to jestesmy w secie 2
            if (scoreSet(set2p1, set2p2)) { //jesli prawda to skonczyl sie 2 set
                if(isEnd(set1p1,set1p2,set2p1,set2p2)){ //jesli prawda to koniec meczu
                    serve1.text = "W" //medal/laur przy zawodniku ktory wygral
                    serve2.text = ""
                    fillUpScore(app,player1, player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    context.startActivity(Intent(context,EndOfMatchActivity::class.java)) //zmiana aktywnosci na EndOfMatch
                }
                else {
                    set3p1.text = "0"
                    set3p2.text = "0"
                }
            }
            else {
                if (set2p1.text == "6" && set2p2.text == "6") { //jesli prawda to bedzie tiebreak
                    app.czyTiebreak = true
                }
            }
        }
        else{ //jestesmy w secie 3.
            if (scoreSet(set3p1, set3p2)) { //jesli prawda to skonczyl sie 3 set
                serve1.text = "W" //medal/laur przy zawodniku ktory wygral
                serve2.text = ""
                fillUpScore(app,player1, player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                context.startActivity(Intent(context,EndOfMatchActivity::class.java)) //zmiana aktywnosci na EndOfMatch
            }
            else {
                if (set3p1.text == "6" && set3p2.text == "6") { //jesli prawda to bedzie tiebreak
                    app.czyTiebreak = true
                }
            }
        }
    }
    if (app.czyTiebreak) {
        val p1_string: String = pkt1.text.toString()
        val p1: Int = p1_string.toInt()
        val p2_string: String = pkt2.text.toString()
        val p2: Int = p2_string.toInt()
        if ((p1 + p2) % 2 != 0) { //zmiana serwujacego w tiebreaku przy nieparzystym wyniku
            if (serve1.text == "") {
                serve1.text = "1"
                serve2.text = ""
            } else {
                serve2.text = "1"
                serve1.text = ""
            }
        }
    }
    fillUpScore(app,player1, player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //tutaj nie wiem czy potrzebne, ale niech narazie zostanie
}

fun scorePkt(pkt1: TextView, pkt2: TextView): Boolean {
    if (pkt1.text == "0") {
        pkt1.text = "15"
    } else if (pkt1.text == "15") {
        pkt1.text = "30"
    } else if (pkt1.text == "30" || pkt1.text == "") {
        pkt1.text = "40"
        if (pkt1.text == "") {
            pkt2.text = "40"
        }
    } else if (pkt1.text == "40" && pkt2.text == "40") {
        pkt1.text = "A"
        pkt2.text = ""
    } else if (pkt1.text == "40" || pkt1.text == "A") { //koniec gema
        pkt1.text = "0"
        pkt2.text = "0"
        return true; //zmiana wyniku w secie
    }
    return false; //dalej gramy gema
}

fun scoreSet(setp1: TextView, setp2: TextView): Boolean {
    val s1_string: String = setp1.text.toString()
    var s1: Int = s1_string.toInt()
    val s2_string: String = setp2.text.toString()
    var s2: Int = s2_string.toInt()

    s1++
    setp1.text = s1.toString()
    if (s1 - 1 == 5) {
        if (s2 != 5 && s2 != 6) {
            return true;
        }
    } else if (s1 - 1 == 6) {
        return true
    }
    return false
}

fun scoreTiebreak(pkt1: TextView, pkt2: TextView): Boolean {
    val p1_string: String = pkt1.text.toString()
    var p1: Int = p1_string.toInt()
    val p2_string: String = pkt2.text.toString()
    var p2: Int = p2_string.toInt()

    p1++
    pkt1.text = p1.toString()
    if (p1 - 1 >= 6 && (p1 - 1 - p2) >= 1) {
        return true;
    }
    return false
}

//spr czy koniec meczu
fun isEnd(set1p1: TextView, set1p2: TextView,set2p1: TextView, set2p2: TextView): Boolean{
    var s1p1: Int = set1p1.text.toString().toInt()
    var s1p2: Int = set1p2.text.toString().toInt()
    var s2p1: Int = set2p1.text.toString().toInt()
    var s2p2: Int = set2p2.text.toString().toInt()

    if(s1p1>s1p2)
    {
        if((s2p1==6 && s2p1-s2p2>1) || s2p1==7){
            return true
        }
    }
    return false
}

//aktualizuje wynik w klasie Stats
fun fillUpScore(app: Stats,player1: TextView, player2: TextView, serve1: TextView, serve2: TextView, pkt1: TextView, pkt2: TextView, set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView): Unit {
    if(player1.text == app.player1) {
        app.serve1 = serve1.text.toString()
        app.serve2 = serve2.text.toString()
        app.pkt1 = pkt1.text.toString()
        app.pkt2 = pkt2.text.toString()
        app.set1p1 = set1p1.text.toString()
        app.set1p2 = set1p2.text.toString()
        app.set2p1 = set2p1.text.toString()
        app.set2p2 = set2p2.text.toString()
        app.set3p1 = set3p1.text.toString()
        app.set3p2 = set3p2.text.toString()
    }
    else
    {
        app.serve2 = serve1.text.toString()
        app.serve1 = serve2.text.toString()
        app.pkt2 = pkt1.text.toString()
        app.pkt1 = pkt2.text.toString()
        app.set1p2 = set1p1.text.toString()
        app.set1p1 = set1p2.text.toString()
        app.set2p2 = set2p1.text.toString()
        app.set2p1 = set2p2.text.toString()
        app.set3p2 = set3p1.text.toString()
        app.set3p1 = set3p2.text.toString()
    }
}