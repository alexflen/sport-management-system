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
    var start: Time? = null
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

class Time(time : String) {
    val H: Int
    val M: Int
    val S: Int
    init {
        val numbers = time.split(':')
        if (numbers.size != 3) {
            throw IllegalStateException("Incorrect time")
        }
        if (numbers.get(0).toIntOrNull() == null) {
            throw IllegalStateException("Incorrect H in time")
        } else {
            this.H = numbers.get(0).toInt()
            if (this.H < 0 || this.H > 23) {
                throw IllegalStateException("Incorrect H in time")
            }
        }

        if (numbers.get(1).toIntOrNull() == null) {
            throw IllegalStateException("Incorrect M in time")
        } else {
            this.M = numbers.get(1).toInt()
            if (this.M < 0 || this.M > 59) {
                throw IllegalStateException("Incorrect M in time")
            }
        }

        if (numbers.get(2).toIntOrNull() == null) {
            throw IllegalStateException("Incorrect S in time")
        } else {
            this.S = numbers.get(2).toInt()
            if (this.S < 0 || this.S > 59) {
                throw IllegalStateException("Incorrect S in time")
            }
        }

    }

    operator fun compareTo(other : Time): Int {
        if (this.H != other.H) {
            return this.H - other.H
        } else if (this.M != other.M) {
            return this.M - other.M
        } else {
            return this.S - other.S
        }
    }

    operator fun Time.minus(other : Time): Time {
        if (this < other) {
            throw IllegalStateException("More is subtracted from less time")
        }
        var h = this.H - other.H
        var m = this.M - other.M
        var s = this.S - other.S
        if (s < 0) {
            s += 60
            m -= 1
        }
        if (m < 0) {
            m += 60
            h -= 1
        }
        return Time("$h:$m:$s")
    }

    operator fun inc(): Time {
        var s = this.S + 1
        var m = this.M
        var h = this.H
        if (s == 60) {
            s = 0
            m += 1
        }
        if (m == 60) {
            m = 0
            h += 1
        }
        if (h == 24) {
            h = 0
        }
        return Time("$h:$m:$s")
    }

    override fun toString(): String {
        return "${"$H".padStart(2, '0')}:${"$M".padStart(2, '0')}:${"$S".padStart(2, '0')}"
    }
}

class Station(name: String, number: Int, time: String) {
    val name: String
    val number: Int
    val time: String
    init {
        this.name = name
        this.number = number
        this.time = time
    }

    fun toStringName(): String {
        return "$name,$time"
    }

    fun toStringNumber(): String {
        return "$number,$time"
    }
}

class StationSportsman(stations: List<Station>) {
    val stations: List<Station>
    init {
        this.stations = stations
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
        groups = doEverythingToMakeGroups(enrolled)
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
                var currentTime = Time("12:00:00")
                for (j in formedGroups[i].participants.indices) {
                    formedGroups[i].participants[j].start = currentTime
                    currentTime++
                }
            }
            return result
        }

        fun doEverythingToMakeGroups(enrolled: List<StartEnrollSportsman>): List<StartGroup> {
            return assignTime(divideInGroups(assignGroup(assignNumbers(enrolled))))
        }
    }
}

fun main(args: Array<String>) {
    TODO()
}
