package com.anw.tenistats.player

class PlayerView {
    val name: String=""
    var player: String=""
    var firstName: String =""
    var lastName: String=""
    var active : Boolean=true
    var team : String=""
    var isFavorite: Boolean = false

    constructor()

    constructor(player: String, firstName: String, lastName: String, active:Boolean, team: String, isFavorite: Boolean)
    {
        this.player=player
        this.firstName=firstName
        this.lastName=lastName
        this.active=active
        this.team=team
        this.isFavorite=isFavorite
    }
}
