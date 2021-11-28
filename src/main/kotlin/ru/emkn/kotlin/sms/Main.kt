package ru.emkn.kotlin.sms

typealias IAE = IllegalArgumentException // Used too often so needs to be typealiased

/*
 * Just a basic Sportsman class for every other Sportsman to inherit the fields
 */
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

/*
 * Unified class for Start and Enroll Sportsman
 * Can be Start with added info such as number and start time and can be Enroll with basic info
 */
open class StartEnrollSportsman(surname: String, name: String, birthYear: Int, collective: String, desiredGroup: String?, number: Int? = null):
    Sportsman(surname, name, birthYear, collective)  {
    val desiredGroup: String?
    var start: Time? = null
    var number: Int?
    init {
        this.desiredGroup = desiredGroup
        this.number = number
    }

    fun toStringEnroll(): String {
        return "${super.toString()},${desiredGroup ?: ""},$collective"
    }

    fun toStringStart(): String {
        return "${number?: ""},${super.toString()},$start"
    }
}

/*
 * Does time logic for this project
 */
class Time(time : String) {
    var H: Int
    var M: Int
    var S: Int
    init {
        val numbers = time.split(':')
        if (numbers.size != 3) {
            throw IllegalStateException("Incorrect time")
        }
        if (numbers[0].toIntOrNull() == null) {
            throw IllegalStateException("Incorrect H in time")
        } else {
            this.H = numbers.get(0).toInt()
            if (this.H < 0 || this.H > 23) {
                throw IllegalStateException("Incorrect H in time")
            }
        }

        if (numbers[1].toIntOrNull() == null) {
            throw IllegalStateException("Incorrect M in time")
        } else {
            this.M = numbers.get(1).toInt()
            if (this.M < 0 || this.M > 59) {
                throw IllegalStateException("Incorrect M in time")
            }
        }

        if (numbers[2].toIntOrNull() == null) {
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

    operator fun minus(other : Time): Time {
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

    fun Seconds(): Int {
        return this.H * 24 * 60 + this.M * 60 + this.S
    }

    operator fun div(other : Time): Double {
        return this.Seconds().toDouble() / other.Seconds()
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
        this.H = h
        this.M = m
        this.S = s
        return Time("$h:$m:$s")
    }

    override fun toString(): String {
        return "${"$H".padStart(2, '0')}:${"$M".padStart(2, '0')}:${"$S".padStart(2, '0')}"
    }
}

/*
 * Stores the information about a sportsman performance on a station
 */
class Station(name: String, number: Int, time: Time) {
    val name: String
    val number: Int
    val time: Time
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

/*
 * Contains a list of station results for a single sportsman
 * Sportsman is identified by his/her number
 */
class StationSportsman(number: Int, stations: List<Station>) {
    val number: Int
    val stations: List<Station>
    init {
        this.number = number
        this.stations = stations

        for (i in 1..this.stations.size - 1) {
            if (this.stations[i - 1].time >= this.stations[i].time) {
                throw IAE("Incorrect time for the passage of the station by the athlete with the number ${this.number}")
            }
        }
    }

    constructor(sportNumber: Int, allStations: ManyStationProtocols): this(sportNumber, filterThis(sportNumber, allStations.stationProtocols))

    override fun toString(): String {
        val result = StringBuilder("$number\n")
        stations.forEach {
            result.appendLine(it.toStringName())
        }
        return result.toString()
    }

    companion object {
        fun filterThis(number: Int, allStations: List<StationProtocol>): List<Station> {
            val result = mutableListOf<Station>()
            allStations.forEach {
                it.sportsmen.forEach { station ->
                    require(it.name == station.name) { "Station names must be equal" }
                    if (station.number == number) {
                        result.add(station)
                    }
                }
            }
            return result
        }
    }
}

/*
 * Contains a protocol for one station
 * Has a station name and a sportsmen results list (stored in Station universal class)
 */
class StationProtocol(name: String, sportsmen: List<Station>) {
    val name: String
    val sportsmen: List<Station>
    init {
        this.name = name
        this.sportsmen = sportsmen
    }

    override fun toString(): String {
        val result = StringBuilder("$name\n")
        sportsmen.forEach {
            result.appendLine(it.toStringNumber())
        }
        return result.toString()
    }
}

data class ManyStationProtocols(val stationProtocols: List<StationProtocol>)

/*
 * A group of sportsmen (may be any of Sportsman classes)
 * Has a name and a list of sportsmen in it
 */
class Group<T: Sportsman>(name: String, participants: List<T>) {
    val name: String
    val participants: List<T>
    init {
        this.name = name
        this.participants = participants
    }

    fun toStringStart(): String {
        val result = StringBuilder("$name\n")
        participants.forEach {
            if (it is StartEnrollSportsman) {
                result.appendLine(it.toStringStart())
            } else {
                throw IAE("Wrong Sportsman; expected StartEnrollSportsman")
            }
        }
        return result.toString()
    }

    fun toStringResult(): String {
        val result = StringBuilder("$name\n")
        participants.forEach {
            if (it is ResultSportsman) {
                result.appendLine(it)
            } else {
                throw IAE("Wrong Sportsman; expected ResultSportsman")
            }
        }
        return result.toString()
    }
}

/*
 * Contains all groups, in which sportsmen are assigned a number and a start time
 */
class AllStartGroups(enrolled: List<StartEnrollSportsman>) {
    val groups: List<Group<StartEnrollSportsman>>
    init {
        groups = doEverythingToMakeGroups(enrolled)
    }

    companion object {
        private fun assignNumbers(enrolled: List<StartEnrollSportsman>): List<StartEnrollSportsman> {
            val shuffled = enrolled.shuffled()
            val result = mutableListOf<StartEnrollSportsman>()
            for (i in shuffled.indices) {
                result.add(shuffled[i])
                result.last().number = i + 1
            }
            return result
        }

        private fun assignGroup(enrolled: List<StartEnrollSportsman>): List<StartEnrollSportsman> {
            return enrolled.map { StartEnrollSportsman(it.surname, it.name,
                    it.birthYear, it.collective, it.desiredGroup?: "unspecified") }
        }

        private fun divideInGroups(enrolled: List<StartEnrollSportsman>): List<Group<StartEnrollSportsman>> {
            return enrolled.groupBy { it.desiredGroup?:
                throw IAE("Group must be assigned") }.map { Group(it.key, it.value) }
        }

        private fun assignTime(formedGroups: List<Group<StartEnrollSportsman>>): List<Group<StartEnrollSportsman>> {
            val result = mutableListOf<Group<StartEnrollSportsman>>()
            for (i in formedGroups.indices) {
                var currentTime = Time("12:00:00")
                for (j in formedGroups[i].participants.indices) {
                    formedGroups[i].participants[j].start = currentTime
                    currentTime++
                }
            }
            return result
        }

        fun doEverythingToMakeGroups(enrolled: List<StartEnrollSportsman>): List<Group<StartEnrollSportsman>> {
            return assignTime(divideInGroups(assignGroup(assignNumbers(enrolled))))
        }
    }
}

/*
 * A sportsman class that has a result: his/her time, place and his certainNumber (because it can't be null now)
 */
class ResultSportsman(surname: String, name: String, birthYear: Int, collective: String, desiredGroup: String, number: Int, time: Time, place: Int? = null):
    StartEnrollSportsman(surname, name, birthYear, collective, desiredGroup) {
    val time: Time
    val place: Int?
    val certainNumber: Int
    init {
        this.time = time
        this.place = place
        this.certainNumber = number
    }

    constructor(sportsman: StartEnrollSportsman, results: StationSportsman): this(sportsman.surname, sportsman.name,
        sportsman.birthYear, sportsman.collective, sportsman.desiredGroup?: throw IAE("A sportsman must have a group"),
        sportsman.number?: throw IAE("A sportsman must have a number"), getTime(sportsman, results))

    override fun toString(): String {
        return "$place,$number,${super.toString()},$time"
    }

    companion object {
        fun getTime(sportsman: StartEnrollSportsman, results: StationSportsman): Time {
            require(sportsman.number == results.number) { "Numbers of sportsman must be equal" }
            return results.stations.last().time - sportsman.start!!
        }
    }
}

/*
 * Contains all individual results of each group
 */
class AllResultGroups(givenGroups: List<Group<ResultSportsman>>) {
    val groups: List<Group<ResultSportsman>>
    init {
        groups = givenGroups
    }

    constructor(startGroups: AllStartGroups, participants: List<ResultSportsman>):
            this(sortAndPlaces(startGroups, participants))

    companion object {
        private fun sortAndPlaces(startGroups: AllStartGroups, participants: List<ResultSportsman>):
                List<Group<ResultSportsman>> {
            val result = mutableListOf<Group<ResultSportsman>>()
            val mapParticipants = participants.groupBy { it.number?:
                throw IAE("Every sportsman must have a number at this point") }
            startGroups.groups.forEach { initGroup ->
                val current: Group<ResultSportsman> = Group(initGroup.name,
                    initGroup.participants.map {
                        ResultSportsman(it.surname, it.name, it.birthYear, it.collective,
             it.desiredGroup?: throw IAE("Every sportsman must have a group at this point"),
                 it.number?: throw IAE("Every sportsman must have a number at this point"),
                        mapParticipants[it.number?: throw IAE("Every sportsman must have a number at this point")]!!.first().time)
                    }.sortedWith { a, b ->
                        a.time.compareTo(b.time)
                    }.mapIndexed { index, it -> ResultSportsman(it.surname, it.name, it.birthYear, it.collective,
             it.desiredGroup?: throw IAE("Every sportsman must have a group at this point"),
                        it.certainNumber, it.time, index + 1) }
                )
                result.add(current)
            }
            return result
        }
    }
    }

/*
 * Contains one collective result: the name of the collective and points
 */
class CollectiveResult(val collective: String, val points: Double) {
    constructor(name: String, results: AllResultGroups): this(name, getPoints(name, results))
    companion object {
        fun getPoints(name: String, results: AllResultGroups): Double {
            var points = 0.toDouble()
            results.groups.forEach { group ->
                var tempPoints = 0.toDouble()
                for (i in group.participants.indices) {
                    require(!(i > 0 && group.participants[i].time < group.participants[i - 1].time)) { "Sportsmen must be sorted" }
                    if (group.participants[i].collective == name) {
                        tempPoints = 100 * (2 - group.participants[i].time / group.participants.first().time)
                        break
                    }
                }
                tempPoints = tempPoints.coerceAtLeast(0.toDouble())
                points += tempPoints
            }

            return points
        }
    }

    override fun toString(): String {
        return "$collective: ${"%.3f".format(points)} points"
    }
}

/*
 * Contains all collective results
 */
class AllCollectiveResults(val results: List<CollectiveResult>) {
    override fun toString(): String {
        val result = StringBuilder("COLLECTIVE RESULTS\n")
        results.forEach {
            result.appendLine(it)
        }
        return result.toString()
    }
}

fun main(args: Array<String>) {
    TODO()
}
