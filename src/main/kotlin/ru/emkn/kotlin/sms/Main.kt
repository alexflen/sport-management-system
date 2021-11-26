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
    Sportsman(surname, name, birthYear, collective) {
    val desiredGroup: String?
    var start: Int? = null
    var number: Int? = null

    init {
        this.desiredGroup = desiredGroup
    }

    fun toStringEnroll(): String {
        return "${super.toString()},${desiredGroup ?: ""},$collective"
    }

    fun toStringStart(): String {
        return "$number,${super.toString()},$start"
    }
}

class StartGroup(name: String, participants: List<StartEnrollSportsman>) {
    val name: String
    val participants: List<StartEnrollSportsman>
    init {
        this.name = name
        this.participants = participants
    }

    override fun toString(): String {
        var result: String = "$name\n"
        participants.forEach { 
            result += "${it.toStringStart()}\n"
        }
        return result
    }
}

class AllGroups(enrolled: List<StartEnrollSportsman>) {
    val groups: List<StartGroup>
    init {

    }

    companion object {
        fun assignNumbers(enrolled: List<StartEnrollSportsman>): List<StartEnrollSportsman> {
            val shuffled = enrolled.shuffled()
            val result = mutableListOf<StartEnrollSportsman>()
            for (i in shuffled.indices) {
                result.add(shuffled[i])
                result.last().number = i + 1
            }
            return result
        }

        fun assignGroup(enrolled: List<StartEnrollSportsman>): List<StartEnrollSportsman> {
            return enrolled.map { it -> StartEnrollSportsman(it.surname, it.name,
                    it.birthYear, it.collective, it.desiredGroup?: "unspecified") }
        }

        fun divideInGroups(enrolled: List<StartEnrollSportsman>): List<StartGroup> {
            return enrolled.groupBy { it.desiredGroup?:
                throw IllegalArgumentException("Group must be assigned") }.map { StartGroup(it.key, it.value) }
        }

        fun assignTime(formedGroups: List<StartGroup>): List<StartGroup> {
            val result = mutableListOf<StartGroup>()
            for (i in formedGroups.indices) {
                val currentTime = Time("12:00:00")

            }
        }
    }
}

fun main(args: Array<String>) {
    TODO()
}
