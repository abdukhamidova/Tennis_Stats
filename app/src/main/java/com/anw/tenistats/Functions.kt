package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

//poczatkowe ustawienie wyniku dla kazdej z aktywnosci z wynikiem
fun clearScore(app: Stats)
{
    app.isEnd = false //czy koniec meczu
    app.serwis = 1 //1-wszedl 1 serwis; 2-wszedl drugi serwis; 0-podwojny blad
    app.pktId = 1 //liczba zagranych punktow w gemie

    app.isTiebreak = false
    //dane
    app.player1 = ""
    app.player2 = ""
    //wynik
    app.serve1 = ""
    app.serve2 = ""
    app.pkt1 = "0"
    app.pkt2 = "0"
    app.set1p1 = "0"
    app.set1p2 = "0"
    app.set2p1 = ""
    app.set2p2 = ""
    app.set3p1 = ""
    app.set3p2 = ""
}

fun fillUpScoreInActivity(app: Stats,player1: TextView, player2: TextView, serve1: TextView, serve2: TextView,
                          pkt1: TextView, pkt2: TextView, set1p1: TextView, set1p2: TextView, set2p1: TextView,
                          set2p2: TextView, set3p1: TextView, set3p2: TextView) {
    player1.text = app.player1
    player2.text = app.player2
    if(app.serve1=="1"){
        serve1.visibility = View.VISIBLE
        serve2.visibility = View.INVISIBLE
    }
    else{
        serve1.visibility = View.INVISIBLE
        serve2.visibility = View.VISIBLE
    }
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
fun score(app: Stats,player1: TextView, serve1: TextView, serve2: TextView, pkt1: TextView, pkt2: TextView, set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView) {
    var serwis1: String
    var serwis2: String
    if(app.player1 == player1.text){
        serwis1 = app.serve1
        serwis2 = app.serve2
    }
    else{
        serwis1 = app.serve2
        serwis2 = app.serve1
    }
    if (app.isTiebreak) { //gramy tiebreaka
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
                fillUpScore(app,player1,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                app.isEnd=true
            }
            pkt1.text = "0"
            pkt2.text = "0"
            if(serwis1 != ""){//zmiana serwujacego
                serve1.visibility = View.INVISIBLE
                serve2.visibility = View.VISIBLE
                serwis1 = ""
                serwis2 = "1"
            }
            else{
                serve1.visibility = View.VISIBLE
                serve2.visibility = View.INVISIBLE
                serwis1 = "1"
                serwis2 = ""
            }
            app.isTiebreak=false
        }
    }
    else if (scorePkt(pkt1, pkt2)) { //jesli prawda to koniec gema
        if(serwis1 != ""){//zmiana serwujacego
            serve1.visibility = View.INVISIBLE
            serve2.visibility = View.VISIBLE
            serwis1 = ""
            serwis2 = "1"
        }
        else{
            serve1.visibility = View.VISIBLE
            serve2.visibility = View.INVISIBLE
            serwis1 = "1"
            serwis2 = ""
        }
        if (set2p1.text == "" && set2p2.text == "") { //jesli prawda to jestesmy w secie 1
            if (scoreSet(set1p1, set1p2)) { //jesli prawda to skonczyl sie 1 set
                set2p1.text = "0"
                set2p2.text = "0"
            }
            else {
                if (set1p1.text == "6" && set1p2.text == "6") { //jesli prawda to bedzie tiebreak
                    app.isTiebreak = true
                }
            }
        }
        else if(set3p1.text == "" && set3p2.text == ""){ //jesli prawda to jestesmy w secie 2
            if (scoreSet(set2p1, set2p2)) { //jesli prawda to skonczyl sie 2 set
                if(isEnd(set1p1,set1p2,set2p1,set2p2)){ //jesli prawda to koniec meczu
                    serwis1 = "W" //medal/laur przy zawodniku ktory wygral
                    serwis2 = ""
                    serve1.visibility = View.VISIBLE
                    serve2.visibility = View.INVISIBLE
                    fillUpScore(app,player1,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    app.isEnd=true
                }
                else {
                    set3p1.text = "0"
                    set3p2.text = "0"
                }
            }
            else {
                if (set2p1.text == "6" && set2p2.text == "6") { //jesli prawda to bedzie tiebreak
                    app.isTiebreak = true
                }
            }
        }
        else{ //jestesmy w secie 3.
            if (scoreSet(set3p1, set3p2)) { //jesli prawda to skonczyl sie 3 set
                serwis1 = "W" //medal/laur przy zawodniku ktory wygral
                serwis2 = ""
                serve1.visibility = View.VISIBLE
                serve2.visibility = View.INVISIBLE
                fillUpScore(app,player1,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                app.isEnd=true
            }
            else {
                if (set3p1.text == "6" && set3p2.text == "6") { //jesli prawda to bedzie tiebreak
                    app.isTiebreak = true
                }
            }
        }
    }
    if (app.isTiebreak) {
        val p1_string: String = pkt1.text.toString()
        val p1: Int = p1_string.toInt()
        val p2_string: String = pkt2.text.toString()
        val p2: Int = p2_string.toInt()
        if ((p1 + p2) % 2 != 0) { //zmiana serwujacego w tiebreaku przy nieparzystym wyniku
            if (serwis1 == "") {
                serwis1 = "1"
                serwis2 = ""
                serve1.visibility = View.VISIBLE
                serve2.visibility = View.INVISIBLE
            } else {
                serwis1 = ""
                serwis2 = "1"
                serve1.visibility = View.INVISIBLE
                serve2.visibility = View.VISIBLE
            }
        }
    }
    if(app.player1 == player1.text){
        app.serve1 = serwis1
        app.serve2 = serwis2
    }
    else{
        app.serve2 = serwis1
        app.serve1 = serwis2
    }
    fillUpScore(app,player1,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //tutaj nie wiem czy potrzebne, ale niech narazie zostanie
}

@SuppressLint("SetTextI18n")
fun scorePkt(pkt1: TextView, pkt2: TextView): Boolean {
    if (pkt1.text == "0") {
        pkt1.text = "15"
    } else if (pkt1.text == "15") {
        pkt1.text = "30"
    } else if (pkt1.text == "30" || pkt1.text == "") {
        if (pkt1.text == "") {
            pkt2.text = "40"
        }
        pkt1.text = "40"
    } else if (pkt1.text == "40" && pkt2.text == "40") {
        pkt1.text = "A"
        pkt2.text = ""
    } else if (pkt1.text == "40" || pkt1.text == "A") { //koniec gema
        pkt1.text = "0"
        pkt2.text = "0"
        return true //zmiana wyniku w secie
    }
    return false //dalej gramy gema
}

fun scoreSet(setp1: TextView, setp2: TextView): Boolean {
    val s1_string: String = setp1.text.toString()
    var s1: Int = s1_string.toInt()
    val s2_string: String = setp2.text.toString()
    val s2: Int = s2_string.toInt()

    s1++
    setp1.text = s1.toString()
    if (s1 - 1 == 5) {
        if (s2 != 5 && s2 != 6) {
            return true
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
    val p2: Int = p2_string.toInt()

    p1++
    pkt1.text = p1.toString()
    if (p1 - 1 >= 6 && (p1 - 1 - p2) >= 1) {
        return true
    }
    return false
}

//spr czy koniec meczu
fun isEnd(set1p1: TextView, set1p2: TextView,set2p1: TextView, set2p2: TextView): Boolean{
    val s1p1: Int = set1p1.text.toString().toInt()
    val s1p2: Int = set1p2.text.toString().toInt()
    val s2p1: Int = set2p1.text.toString().toInt()
    val s2p2: Int = set2p2.text.toString().toInt()

    if(s1p1>s1p2)
    {
        if((s2p1==6 && s2p1-s2p2>1) || s2p1==7){
            return true
        }
    }
    return false
}

//aktualizuje wynik w klasie Stats
fun fillUpScore(app: Stats,player1: TextView, pkt1: TextView, pkt2: TextView,
                set1p1: TextView, set1p2: TextView, set2p1: TextView,
                set2p2: TextView, set3p1: TextView, set3p2: TextView){
    if(player1.text == app.player1) {
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
fun fillUpScoreUndoCustom(app: Stats,player1: String, pkt1: String, pkt2: String,
                set1p1: String, set1p2: String, set2p1: String,
                set2p2: String, set3p1: String, set3p2: String){
    if(player1 == app.player1) {
        app.pkt1 = pkt1
        app.pkt2 = pkt2
        app.set1p1 = set1p1
        app.set1p2 = set1p2
        app.set2p1 = set2p1
        app.set2p2 = set2p2
        app.set3p1 = set3p1
        app.set3p2 = set3p2
    }
    else
    {
        app.pkt2 = pkt1
        app.pkt1 = pkt2
        app.set1p2 = set1p1
        app.set1p1 = set1p2
        app.set2p2 = set2p1
        app.set2p1 = set2p2
        app.set3p2 = set3p1
        app.set3p1 = set3p2
    }
}

fun getGoldenDrawable(context: Context, drawableId: Int): Drawable {
    // Pobierz obrazek zasobu
    val drawable = ContextCompat.getDrawable(context, drawableId)

    // Utwórz kopię obrazka z filtrem koloru
    val wrappedDrawable = DrawableCompat.wrap(drawable!!)
    DrawableCompat.setTint(wrappedDrawable.mutate(), ContextCompat.getColor(context, R.color.gold))
    DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)

    return wrappedDrawable
}