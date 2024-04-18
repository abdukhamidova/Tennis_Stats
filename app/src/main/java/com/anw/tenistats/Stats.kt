package com.anw.tenistats

import android.app.Application

class Stats : Application() {
    var isCanceled : Boolean = false
    var isEnd: Boolean = false //czy koniec meczu
    var serwis: Int = 1 //1-wszedl 1 serwis; 2-wszedl drugi serwis; 0-podwojny blad
    var pktId: Int = 1 //liczba zagranych punktow w meczu

    var czyTiebreak: Boolean = false
    //dane
    var player1: String = ""
    var player2: String = ""
    //wynik
    var serve1: String = ""
    var serve2: String = ""
    var pkt1: String = "0"
    var pkt2: String = "0"
    var set1p1: String = "0"
    var set1p2: String = "0"
    var set2p1: String = ""
    var set2p2: String = ""
    var set3p1: String = ""
    var set3p2: String = ""

    //--reszta raczej nie bedzie uzywana

    //zmienne globalne do liczenia statystyk
    var totalpoints1: Int = 0   //ostatecznie kto ile wygrał punktów
    var totalpoints2: Int = 0

    var firstserve1: Int = 0 //ilosc ogolnie zagranych 1 serwisow (firstservein1 + secondserve1)
    var firstserve2: Int = 0
    var firstservein1: Int = 0 //ilosc trafionych 1 serwisow (w procentach: firstservein1/firstserve1 * 100%)
    var firstservein2: Int = 0

    var secondserve1: Int = 0 //ilosc ogolnie zagranych 2 serwisow (secondservein1 + doublefault1)
    var secondserve2: Int = 0
    var secondservein1: Int = 0 //ilosc trafionych 2 serwisow (w procentach: secondservein1/secondserve1 * 100%)
    var secondservein2: Int = 0

    var ace1: Int = 0 //ilosc zagranych asow playera 1
    var ace2: Int = 0
    var doublefault1: Int = 0 //ilosc podwojnych bledow
    var doublefault2: Int = 0

    var returnwinnerFH1: Int = 0
    var returnwinnerFH2: Int = 0
    var returnwinnerBH1: Int = 0
    var returnwinnerBH2: Int = 0

    var returnerrorFH1: Int = 0
    var returnerrorFH2: Int = 0
    var returnerrorBH1: Int = 0
    var returnerrorBH2: Int = 0

    var winnergroundFH1: Int = 0
    var winnergroundFH2: Int = 0
    var winnergroundBH1: Int = 0
    var winnergroundBH2: Int = 0

    var winnersliceFH1: Int = 0
    var winnersliceFH2: Int = 0
    var winnersliceBH1: Int = 0
    var winnersliceBH2: Int = 0

    var winnervolleyFH1: Int = 0
    var winnervolleyFH2: Int = 0
    var winnervolleyBH1: Int = 0
    var winnervolleyBH2: Int = 0

    var winnersmashFH1: Int = 0
    var winnersmashFH2: Int = 0
    var winnersmashBH1: Int = 0
    var winnersmashBH2: Int = 0

    var winnerlobFH1: Int = 0
    var winnerlobFH2: Int = 0
    var winnerlobBH1: Int = 0
    var winnerlobBH2: Int = 0

    var winnerdropshotFH1: Int = 0
    var winnerdropshotFH2: Int = 0
    var winnerdropshotBH1: Int = 0
    var winnerdropshotBH2: Int = 0

    var forcederrorgroundFH1: Int = 0
    var forcederrorgroundFH2: Int = 0
    var forcederrorgroundBH1: Int = 0
    var forcederrorgroundBH2: Int = 0

    var forcederrorsliceFH1: Int = 0
    var forcederrorsliceFH2: Int = 0
    var forcederrorsliceBH1: Int = 0
    var forcederrorsliceBH2: Int = 0

    var forcederrorvolleyFH1: Int = 0
    var forcederrorvolleyFH2: Int = 0
    var forcederrorvolleyBH1: Int = 0
    var forcederrorvolleyBH2: Int = 0

    var forcederrorsmashFH1: Int = 0
    var forcederrorsmashFH2: Int = 0
    var forcederrorsmashBH1: Int = 0
    var forcederrorsmashBH2: Int = 0

    var forcederrorlobFH1: Int = 0
    var forcederrorlobFH2: Int = 0
    var forcederrorlobBH1: Int = 0
    var forcederrorlobBH2: Int = 0

    var forcederrordropshotFH1: Int = 0
    var forcederrordropshotFH2: Int = 0
    var forcederrordropshotBH1: Int = 0
    var forcederrordropshotBH2: Int = 0

    var unforcederrorgroundFH1: Int = 0
    var unforcederrorgroundFH2: Int = 0
    var unforcederrorgroundBH1: Int = 0
    var unforcederrorgroundBH2: Int = 0

    var unforcederrorsliceFH1: Int = 0
    var unforcederrorsliceFH2: Int = 0
    var unforcederrorsliceBH1: Int = 0
    var unforcederrorsliceBH2: Int = 0

    var unforcederrorvolleyFH1: Int = 0
    var unforcederrorvolleyFH2: Int = 0
    var unforcederrorvolleyBH1: Int = 0
    var unforcederrorvolleyBH2: Int = 0

    var unforcederrorsmashFH1: Int = 0
    var unforcederrorsmashFH2: Int = 0
    var unforcederrorsmashBH1: Int = 0
    var unforcederrorsmashBH2: Int = 0

    var unforcederrorlobFH1: Int = 0
    var unforcederrorlobFH2: Int = 0
    var unforcederrorlobBH1: Int = 0
    var unforcederrorlobBH2: Int = 0

    var unforcederrordropshotFH1: Int = 0
    var unforcederrordropshotFH2: Int = 0
    var unforcederrordropshotBH1: Int = 0
    var unforcederrordropshotBH2: Int = 0
}