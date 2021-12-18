package ru.emkn.kotlin.sms

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File


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

fun String.toTimeOrNull(): Time? {
    return try {
        Time(this)
    }
    catch (e: IllegalArgumentException){
        null
    }
}

fun loadCSVToFile(s: String, exportFileName: String) {
    val file = File(exportFileName)
    file.writeText(s)
}

fun loadFromCSVFile(importFileName: String): String {
    var currentName = ""
    val result = StringBuilder()
    csvReader().open(importFileName) {
        readAllAsSequence().forEach { row: List<String> ->
            if (row[1].isEmpty()) {
                currentName = row.first()
            } else {
                result.appendLine((currentName + row.joinToString(prefix = if (currentName.isNotEmpty()) "," else "", separator = ",")).trim())
            }
        }
    }
    return result.toString().trim()
}

fun transformationToStartSportsman(a: List<EnrollSportsman>): List<StartSportsman> {
    return AllStartGroups(a, 0).groups.flatMap {it.participants}
}

fun generateResults(groups: List<StartSportsman>, marks: List<StationPerformance>): List<ResultSportsman> {
    val allStartGroups = AllStartGroups(groups, false)
    val resultSportsmen = groups.map { ResultSportsman(it, StationSportsman(it.number, ManyStationProtocols(marks))) }
    val allResults = AllResultGroups(allStartGroups, resultSportsmen)
    return allResults.groups.flatMap { it.participants }
}

fun checkCorrelation(groups: List<StartSportsman>, dist: List<Station>, marks: List<StationPerformance>): Report {
    if (groups.isEmpty())
        return Report(States.EMPTY, "Groups info is empty")

    if (dist.isEmpty())
        return Report(States.EMPTY, "Dist info is empty")

    if (marks.isEmpty())
        return Report(States.EMPTY, "Marks info is empty")

    val groupsGroups = groups.distinctBy { it.group }.map { it.group }.sorted()
    val distGroups = dist.distinctBy { it.group }.map { it.group }.sorted()
    if (groupsGroups != distGroups)
        return Report(States.WRONG, "Groups in Groups tab and Dist tab do not match")

    groups.forEach { sportsman ->
        val mark = marks.filter { it.number == sportsman.number }
        if (mark.isEmpty())
            return Report(States.WRONG, "Sportsman ${sportsman.number} not found in Marks")
        val distance = dist.filter { it.group == sportsman.group }
        if (mark.map { it.name }.sorted() != (distance.map { it.name } + "finish").sorted())
            return Report(States.WRONG, "Stations of sportsman ${sportsman.number} are not equal to stations in their group")
    }

    return Report(States.OK)
}

fun checkEquals(a: List<StartSportsman>, b: List<EnrollSportsman>): Report {
    val allA = a.map { it.mainInformation() }.toSet()
    val allB = b.map { it.mainInformation() }.toSet()
    if (allA.size != a.size || allB.size != b.size) {
        return Report(States.WRONG, "A sportsman is announced several times")
    }
    else if (allA != allB) {
        return Report(States.WRONG, "Sportsmen lists do not match")
    }
    else {
        return Report(States.OK)
    }
}