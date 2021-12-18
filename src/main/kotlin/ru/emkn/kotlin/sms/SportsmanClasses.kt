package ru.emkn.kotlin.sms


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

    fun mainInformation(): String {
        return "$surname,$name,$birthYear,$collective"
    }

    override fun toString(): String {
        return "$surname,$name,$birthYear,${category?: ""}"
    }
}

/*
 * Enroll Sportsman
 * Enroll with basic info
 */
open class EnrollSportsman(surname: String, name: String, birthYear: Int,
                           collective: String, category: String?, desiredGroup: String = ""):
    Sportsman(surname, name, birthYear, collective, category) {
    val desiredGroup: String
    init {
        this.desiredGroup = desiredGroup
    }

    //for CSV
    constructor(split: List<String>): this(split[1], split[2], split[3].toInt(), split[0], split[4], split[5])

    override fun toString(): String {
        return "$collective,${super.toString()},${desiredGroup}"
    }
}

open class StartSportsman(surname: String, name: String, birthYear: Int, collective: String, category: String?,
                          group: String, number: Int):
    Sportsman(surname, name, birthYear, collective, category) {
    val number: Int
    val group: String
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
                throw IllegalArgumentException("Incorrect time for the passage of the station by the athlete with the number ${this.number}")
            }
        }
    }

    constructor(sportNumber: Int, allStations: ManyStationProtocols): this(sportNumber, filterThis(sportNumber,
        allStations.stationProtocols))

    override fun toString(): String {
        val result = StringBuilder()
        stations.forEach {
            result.appendLine("$number,${it.toStringName()}")
        }
        return result.toString()
    }

    companion object {
        fun filterThis(number: Int, allStations: List<StationPerformance>): List<StationPerformance> {
            return allStations.filter { it.number == number }
        }
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
                throw IllegalArgumentException("Wrong Sportsman; expected StartSportsman")
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
                throw IllegalArgumentException("Wrong Sportsman; expected ResultSportsman")
            }
        }
        return result.toString()
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
