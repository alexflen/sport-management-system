package ru.emkn.kotlin.sms

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
    constructor(split: List<String>): this(split[1], split[0].toInt(), Time(split[2]))

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

data class ManyStationProtocols(val stationProtocols: List<StationPerformance>)


class Station(val group: String, val name: String) {
    //for CSV
    constructor(split: List<String>): this(split[0], split[1])

    override fun toString(): String {
        return "$group,$name"
    }
}

/*
 * Contains all groups, in which sportsmen are assigned a number and a start time
 */
class AllStartGroups(groups: List<Group<StartSportsman>>) {
    val groups: List<Group<StartSportsman>>
    init {
        this.groups = groups
    }

    constructor(starts: List<StartSportsman>, useless: Boolean): this(starts.groupBy { it.group }.map { Group(it.key, it.value) })
    constructor(enrolled: List<EnrollSportsman>, useless: Int): this(doEverythingToMakeGroups(enrolled))

    companion object {
        private fun assignNumbersAndGroups(enrolled: List<EnrollSportsman>): List<StartSportsman> {
            val shuffled = enrolled.shuffled()
            val result = mutableListOf<StartSportsman>()
            for (i in shuffled.indices) {
                result.add(StartSportsman(shuffled[i],
                    shuffled[i].desiredGroup.ifEmpty { "Unspecified" }, i + 1))
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
                result.add(formedGroups[i])
            }
            return result
        }

        fun doEverythingToMakeGroups(enrolled: List<EnrollSportsman>): List<Group<StartSportsman>> {
            return assignTime(divideInGroups(assignNumbersAndGroups(enrolled)))
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