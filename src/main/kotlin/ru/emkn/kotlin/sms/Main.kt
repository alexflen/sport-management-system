package ru.emkn.kotlin.sms

open class Sportsman(surname: String, name: String, birthYear: Int, collective: String, category: String? = null) {
    val surname: String
    val name: String
    val birthYear: Int
    var collective: String
    var category: String?
    init {
        this.surname = surname
        this.name = name
        this.birthYear = birthYear
        this.collective = collective
        this.category = category
    }

    override fun toString(): String {
        return "$surname,$name,$birthYear,${category?: ""}"
    }
}

class StartEnrollSportsman(surname: String, name: String, birthYear: Int, collective: String, desiredGroup: String?):
    Sportsman(surname, name, birthYear, collective)  {
    val desiredGroup: String?
    var start: Int? = null
    var number: Int? = null
    init {
        this.desiredGroup = desiredGroup
    }

    fun toStringEnroll(): String {
        return "${super.toString()},${desiredGroup?: ""},$collective"
    }

    fun toStringStart(): String {
        return "$$number,${super.toString()},$collective,$start"
    }
}

class StartGroup(name: String, participants: List<StartEnrollSportsman>) {
    val name: String
    val participants: List<StartEnrollSportsman>
    init {
        this.name = name
        this.participants = participants
    }
}

fun main(args: Array<String>) {
    TODO()
}
