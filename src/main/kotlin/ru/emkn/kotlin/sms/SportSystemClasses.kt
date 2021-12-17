package ru.emkn.kotlin.sms

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.util.*

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
open class EnrollSportsman(surname: String, name: String, birthYear: Int,
                           collective: String, category: String?, desiredGroup: String?):
    Sportsman(surname, name, birthYear, collective, category) {
    val desiredGroup: String?
    init {
        this.desiredGroup = desiredGroup
    }

    //for CSV
    constructor(split: List<String>): this(split[1], split[2], split[3].toInt(), split[0], split[4], split[5])

    override fun toString(): String {
        return "$collective,${super.toString()},${desiredGroup ?: ""}"
    }
}

open class StartSportsman(surname: String, name: String, birthYear: Int, collective: String, category: String?,
                          group: String, number: Int):
    Sportsman(surname, name, birthYear, collective, category) {
    val number: Int
    val group: String
    lateinit var distanceName: String
    lateinit var start: Time
    init {
        this.number = number
        this.group = group
    }

    constructor(enrolled: EnrollSportsman, group: String, number: Int): this(enrolled.surname, enrolled.name, enrolled.birthYear,
        enrolled.collective, enrolled.category, group, number)

    constructor(surname: String, name: String, birthYear: Int, collective: String, category: String?,
                group: String, number: Int, time: Time): this(surname, name, birthYear, collective, category, group, number) {
                    this.start = time
                }

    //for CSV
    constructor(split: List<String>): this(split[2], split[3], split[4].toInt(), split[6], split[5],
        split[0], split[1].toInt(), Time(split[7]))

    override fun toString(): String {
        return "$group,$number,${super.toString()},$collective,$start"
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
            throw IllegalArgumentException("Incorrect time")
        }
        if (numbers[0].toIntOrNull() == null) {
            throw IllegalArgumentException("Incorrect H in time")
        } else {
            this.H = numbers.get(0).toInt()
            if (this.H < 0 || this.H > 23) {
                throw IllegalArgumentException("Incorrect H in time")
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

fun String.toTimeOrNull(): Time? {
    return try {
        Time(this)
    }
    catch (e: IllegalArgumentException){
        null
    }
}

/*
 * Stores the information about a sportsman performance on a station
 */
class StationPerformance(name: String, number: Int, time: Time) {
    val name: String
    val number: Int
    val time: Time
    init {
        this.name = name
        this.number = number
        this.time = time
    }

    //for CSV
    constructor(split: List<String>): this(split[0], split[1].toInt(), Time(split[2]))

    fun toStringName(): String {
        return "$name,$time"
    }

    fun toStringNumber(): String {
        return "$number,$time"
    }

    override fun toString(): String {
        return "$number,$name,$time"
    }
}

/*
 * Contains a list of station results for a single sportsman
 * Sportsman is identified by his/her number
 */
class StationSportsman(number: Int, stations: List<StationPerformance>) {
    val number: Int
    val stations: List<StationPerformance>
    init {
        this.number = number
        this.stations = stations

        for (i in 1 until this.stations.size) {
            if (this.stations[i - 1].time >= this.stations[i].time) {
                throw IAE("Incorrect time for the passage of the station by the athlete with the number ${this.number}")
            }
        }
    }

    constructor(sportNumber: Int, allStations: ManyStationProtocols): this(sportNumber, filterThis(sportNumber, allStations.stationProtocols))

    override fun toString(): String {
        val result = StringBuilder()
        stations.forEach {
            result.appendLine("$number,${it.toStringName()}")
        }
        return result.toString()
    }

    companion object {
        fun filterThis(number: Int, allStations: List<StationProtocol>): List<StationPerformance> {
            val result = mutableListOf<StationPerformance>()
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
class StationProtocol(name: String, sportsmen: List<StationPerformance>) {
    val name: String
    val sportsmen: List<StationPerformance>
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


class Station(val group: String, val name: String) {
    //for CSV
    constructor(split: List<String>): this(split[0], split[1])

    override fun toString(): String {
        return "$group,$name"
    }
}

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
            if (it is StartSportsman) {
                result.appendLine(it.toString())
            } else {
                throw IAE("Wrong Sportsman; expected StartSportsman")
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
class AllStartGroups(enrolled: List<EnrollSportsman>) {
    val groups: List<Group<StartSportsman>>
    init {
        groups = doEverythingToMakeGroups(enrolled)
    }

    companion object {
        private fun assignNumbersAndGroups(enrolled: List<EnrollSportsman>): List<StartSportsman> {
            val shuffled = enrolled.shuffled()
            val result = mutableListOf<StartSportsman>()
            for (i in shuffled.indices) {
                result.add(StartSportsman(shuffled[i],
                    shuffled[i].desiredGroup?: "unspecified", i + 1))
            }
            return result
        }

        private fun divideInGroups(sportsmen: List<StartSportsman>): List<Group<StartSportsman>> {
            return sportsmen.groupBy { it.group }.map { Group(it.key, it.value) }
        }

        private fun assignTime(formedGroups: List<Group<StartSportsman>>): List<Group<StartSportsman>> {
            val result = mutableListOf<Group<StartSportsman>>()
            for (i in formedGroups.indices) {
                var currentTime = Time("12:00:00")
                for (j in formedGroups[i].participants.indices) {
                    formedGroups[i].participants[j].start = currentTime
                    currentTime++
                }
            }
            return result
        }

        fun doEverythingToMakeGroups(enrolled: List<EnrollSportsman>): List<Group<StartSportsman>> {
            return assignTime(divideInGroups(assignNumbersAndGroups(enrolled)))
        }
    }
}

/*
 * A sportsman class that has a result: his/her time, place and his certainNumber (because it can't be null now)
 */
class ResultSportsman(surname: String, name: String, birthYear: Int, collective: String, category: String?, group: String, number: Int, time: Time, place: Int):
    StartSportsman(surname, name, birthYear, collective, category, group, number) {
    val place: Int
    val time: Time
    init {
        this.time = time
        this.place = place
    }

    constructor(sportsman: StartSportsman, results: StationSportsman):
            this(sportsman.surname, sportsman.name,
        sportsman.birthYear, sportsman.collective, sportsman.category, sportsman.group,
        sportsman.number, getTime(sportsman, results), 0)

    //for CSV
    constructor(split: List<String>): this(split[3], split[4], split[5].toInt(), split[6], split[7], split[0],
        split[2].toInt(), Time(split[8]), split[1].toInt())

    override fun toString(): String {
        return "$group,$place,$number,$surname,$name,$birthYear,${category?: ""},$time"
    }

    companion object {
        fun getTime(sportsman: StartSportsman, results: StationSportsman): Time {
            require(sportsman.number == results.number) { "Numbers of sportsman must be equal" }
            return results.stations.last().time - sportsman.start
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
            val mapParticipants = participants.groupBy { it.number }.mapValues { it.value.first() }
            startGroups.groups.forEach { initGroup ->
                result.add(Group(initGroup.name,
                    initGroup.participants.map {
                        ResultSportsman(it.surname, it.name, it.birthYear, it.collective,
                            it.category, it.group, it.number,
                            mapParticipants[it.number]!!.time, 0)
                    }.sortedWith { a, b ->
                        a.time.compareTo(b.time)
                    }.mapIndexed { index, it -> ResultSportsman(it.surname, it.name, it.birthYear, it.collective,
                        it.category, it.group, it.number, it.time, index + 1) }
                ))
            }
            return result
        }
    }
}

/*
 * Contains one collective result: the name of the collective and points
 */
class CollectiveResult(val collective: String, val points: Double, val place: Int? = null) {
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
        return "${place?: ""}. $collective: ${"%.3f".format(points)} points"
    }
}

/*
 * Contains all collective results
 */
class AllCollectiveResults(results: List<CollectiveResult>) {
    val results: List<CollectiveResult>
    init {
        this.results = results.sortedBy { it.points }.mapIndexed { index, collectiveResult ->
            CollectiveResult(collectiveResult.collective, collectiveResult.points, index + 1) }
    }

    override fun toString(): String {
        val result = StringBuilder("COLLECTIVE RESULTS\n")
        results.forEach {
            result.appendLine(it)
        }
        return result.toString()
    }
}

fun sortTableBy(table: List<List<String>>, column: Int, columnType: ColumnTypes): List<List<String>> {
    return table.sortedWith() {
        a, b -> when (columnType) {
            ColumnTypes.INT -> a[column].toInt().compareTo(b[column].toInt())
            ColumnTypes.STRING -> a[column].compareTo(b[column])
            ColumnTypes.TIME -> Time(a[column]).compareTo(Time(b[column]))
        }
    }
}

fun sortTableByDescending(table: List<List<String>>, column: Int, columnType: ColumnTypes): List<List<String>> {
    return sortTableBy(table, column, columnType).reversed()
}

fun filterTableBy(table: List<List<String>>, column: Int, equalTo: String): List<List<String>> {
    return table.filter {
        it[column] == equalTo
    }
}