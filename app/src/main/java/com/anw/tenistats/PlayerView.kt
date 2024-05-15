package com.anw.tenistats

class PlayerView {
    var player: String=""
    var firstName: String =""
    var lastName: String=""
    var active : Boolean=true

    constructor()

    constructor(player: String, firstName: String, lastName: String, active:Boolean)
    {
        this.player=player
        this.firstName=firstName
        this.lastName=lastName
        this.active=active
    }
}
