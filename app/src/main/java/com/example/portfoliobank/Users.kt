package com.example.portfoliobank

class Users{
    var firstName: String? = null
    var lastName: String? = null
    var ProfileImage: String? = null
    var state:String? = null
    var email:String? = null

    constructor():this("","","","","")

    constructor(firstName: String?, lastName: String?, profileimage: String?,state : String?, email : String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.ProfileImage = profileimage
        this.state = state
        this.email = email
    }


}