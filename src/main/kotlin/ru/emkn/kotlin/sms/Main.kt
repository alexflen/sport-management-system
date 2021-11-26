package ru.emkn.kotlin.sms
import java.lang.IllegalStateException

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
            if (this.H < 0) {
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

    operator fun Time.compareTo(other : Time): Int {
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

    override fun toString(): String {
        return "$H:$M:$S"
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
