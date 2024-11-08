package com.anw.tenistats

class PlayerView {
    val name: String=""
    var player: String=""
    var firstName: String =""
    var lastName: String=""
    var active : Boolean=true
    var team : String=""

    constructor()

    constructor(player: String, firstName: String, lastName: String, active:Boolean, team: String)
    {
        this.player=player
        this.firstName=firstName
        this.lastName=lastName
        this.active=active
        this.team=team
    }
}
