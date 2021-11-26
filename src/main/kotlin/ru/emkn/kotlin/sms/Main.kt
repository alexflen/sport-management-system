package ru.emkn.kotlin.sms

open class Sportsman(surname: String, name: String, birthYear: Int, category: String? = null) {
    val surname: String
    val name: String
    val birthYear: Int
    var category: String?
    init {
        this.surname = surname
        this.name = name
        this.birthYear = birthYear
        this.category = category
    }

    override fun toString(): String {
        return "$surname,$name,$birthYear,${category?: ""}"
    }
}

class StartSportsman(surname: String, name: String, birthYear: Int, number: Int, start: String, category: String? = null) :
    Sportsman(surname, name, birthYear, category) {
    val start: String
    val number: Int
    init {
        this.start = start
        this.number = number
    }

    override fun toString(): String {
        return "$number,${super.toString()},$start"
    }
}




fun main(args: Array<String>) {
    TODO()
}
