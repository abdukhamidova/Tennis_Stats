package com.anw.tenistats


data class Match(
    val row: Row = Row()
)

data class Row(
    val set1: MutableList<String> = mutableListOf(),
    val set2: MutableList<String> = mutableListOf(),
    val set3: MutableList<String> = mutableListOf(),
    val wynik: MutableList<String> = mutableListOf(),
    val kto: MutableList<String> = mutableListOf(),
    val co: MutableList<String> = mutableListOf(),
    val czym: MutableList<String> = mutableListOf(),
    val gdzie: MutableList<String> = mutableListOf(),
    val serwis: MutableList<String> = mutableListOf(),
)
