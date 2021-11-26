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

class EnrollSportsman(surname: String, name: String, birthYear: Int, collective: String, category: String? = null, desiredGroup: String? = null):
    Sportsman(surname, name, birthYear, collective, category)  {
    val desiredGroup: String?
    init {
        this.desiredGroup = desiredGroup
    }

    override fun toString(): String {
        return "${super.toString()},${desiredGroup?: ""},$collective"
    }
}

class StartSportsman(surname: String, name: String, birthYear: Int, number: Int, start: String, collective: String, category: String? = null) :
    Sportsman(surname, name, birthYear, collective, category) {
    val start: String
    val number: Int
    init {
        this.start = start
        this.number = number
    }

    override fun toString(): String {
        return "$number,${super.toString()},$collective,$start"
    }
}

class StartGroup(name: String, participants: List<StartSportsman>) {
    val name: String
    val participants: List<StartSportsman>
    init {
        this.name = name
        this.participants = participants
    }
}


fun main(args: Array<String>) {
    TODO()
}
