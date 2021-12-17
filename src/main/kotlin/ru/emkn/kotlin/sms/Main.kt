package ru.emkn.kotlin.sms

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.util.*

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

enum class TabTypes(val title: String, val header: List<String>) {
    TEAMS("Teams", listOf("Team", "Surname", "Name", "BirthYear", "Category", "Group")), // EnrollSposrtsman
    GROUPS("Groups", listOf("Group", "Number", "Surname", "Name", "BirthYear", "Category", "Collective", "StartTime")), // StartSportsman
    DIST("Distances", listOf("Group", "CheckPointName")), // Station
    //PARTICIPANTS("Participants", listOf("Surname", "Name", "BirthYear", "Team", "Category")),
    MARKS("Marks on check points", listOf("Number", "NameOfPoint", "Mark(Time)")), //StationPerformance
    RESULTS("Results", listOf("Group", "Place", "Number", "Surname", "Name", "BirthYear", "Collective", "Category", "Time")) // ResultSportsman
}

enum class ColumnTypes {
    INT, STRING, TIME
}

enum class States {
    OK, WRONG, EMPTY
}

data class Report(val state: States, val message: String = "")

class TabInfo<T>(initCSV: MutableState<String>,
              plainCSV: MutableState<String>,
              importFileName: MutableState<String>,
              exportFileName: MutableState<String>,
              warning: MutableState<String>,
              expandSortChoice: MutableState<Boolean>,
              state: MutableState<States>,
              initLines: MutableState<List<T>>,
              csvLines: MutableState<List<T>>,
              constructor: (List<String>) -> T) {
    //var state = States.EMPTY
    //var queryLines = listOf<String>()
    //var csvLines = listOf<List<String>>()
    var initCSV: MutableState<String>
    var plainCSV: MutableState<String>
    var importFileName: MutableState<String>
    var exportFileName: MutableState<String>
    var warning: MutableState<String>
    var expandSortChoice: MutableState<Boolean>
    var state: MutableState<States>
    var initLines: MutableState<List<T>>
    var csvLines: MutableState<List<T>>
    val constructor: (List<String>) -> T
    init {
        this.initCSV = initCSV
        this.plainCSV = plainCSV
        this.csvLines = csvLines
        this.expandSortChoice = expandSortChoice
        this.exportFileName = exportFileName
        this.importFileName = importFileName
        this.state = state
        this.warning = warning
        this.initLines = initLines
        this.constructor = constructor
    }

    fun checkIfOkCSV(rows: List<List<String>>): Report {
        // Check if all have the same number of columns
        if (rows.isEmpty()) return Report(States.EMPTY, "Warning: empty CSV")
        val headerSize = rows[0].size
        rows.forEachIndexed { index, it ->
            if (it.size != headerSize) {
                return when {
                    it.size < headerSize -> Report(States.WRONG, "Warning: row $index contains less columns than $headerSize")
                    else -> Report(States.WRONG, "Warning: row $index contains more columns than $headerSize")
                }
            }
        }

        return Report(States.OK)
    }

    fun updateWhenCSV(text: String) {
        val table = text.split("\n").map { it.split(',') }
        plainCSV.value = text
        val report = checkIfOkCSV(table)
        state.value = report.state
        warning.value = report.message
    }

    fun overrideClassValues(): Report {
        require(state.value == States.OK)
        val table = plainCSV.value.split("\n").map { it.split(',') }
        try { csvLines.value = table.map { constructor(it) } }
        catch (e : Exception) { return Report(States.WRONG, "Wrong format; can't process") }

        return Report(States.OK)
    }

    private fun getTextLinesFromT(): List<List<String>> {
        return csvLines.value.map {
            it.toString().split(",")
        }
    }

    private fun getColumnType(table: List<List<String>>, column: Int): ColumnTypes {
        var result = ColumnTypes.INT
        table.forEach {
            if (result == ColumnTypes.INT && it[column].toIntOrNull() == null) {
                result = ColumnTypes.TIME
            }
            if (result == ColumnTypes.TIME && it[column].toTimeOrNull() == null) {
                result = ColumnTypes.STRING
            }
        }
        return result
    }

    fun sortByField(index: Int) {
        require(state.value == States.OK)
        val table = getTextLinesFromT()
        if (table.isNotEmpty())
            require(index >= 0 && index < table[0].size)
        val columnType = getColumnType(table, index)
        plainCSV.value = sortTableBy(table, index, columnType).joinToString("\n") { it.joinToString(",") }
    }
}

@Composable
@Preview
fun myApplication(width: Dp, height: Dp) {
    MaterialTheme {
        val currentTab = remember { mutableStateOf(TabTypes.TEAMS) }
        val lstStr = listOf("a", "b")
        val tabInfos = mapOf( TabTypes.TEAMS to TabInfo<EnrollSportsman>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }) { EnrollSportsman(lstStr) },
            TabTypes.GROUPS to TabInfo<StartSportsman>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }) { StartSportsman(lstStr) },
            TabTypes.DIST to TabInfo<Station>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }) { Station(lstStr) },
            TabTypes.MARKS to TabInfo<StationPerformance>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }) { StationPerformance(lstStr) },
            TabTypes.RESULTS to TabInfo<ResultSportsman>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }) { ResultSportsman(lstStr) },
            )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                TabTypes.values().forEach { iterated ->
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                        currentTab.value = iterated
                    }, colors = if (currentTab.value == iterated) ButtonDefaults.buttonColors(Color(0xffffb954))
                    else ButtonDefaults.buttonColors(Color.Unspecified)) { Text(iterated.title) }
                }
            }
            Text(currentTab.value.title, color = Color.Blue, fontSize = 25.sp)
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(500.dp)
                    ) {
                        TextField(
                            value = tabInfos[currentTab.value]!!.importFileName.value,
                            onValueChange = { tabInfos[currentTab.value]!!.importFileName.value = it },
                            label = { Text("Import from file") },
                            modifier = Modifier.width(400.dp)
                        )
                        Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                            tabInfos[currentTab.value]!!.updateWhenCSV(
                                loadFromCSVFile(
                                    currentTab,
                                    tabInfos[currentTab.value]!!.importFileName.value
                                )
                            )
                        }) { Icon(Icons.Rounded.Add, "Import") }
                    }

                    TextField(value = tabInfos[currentTab.value]!!.plainCSV.value,
                        onValueChange = { tabInfos[currentTab.value]!!.updateWhenCSV(it) },
                        modifier = Modifier
                            .height(500.dp)
                            .width(500.dp),
                        label = { Text("${currentTab.value.title} csv info") })
                    Text(tabInfos[currentTab.value]!!.warning.value, color = Color.Red, fontSize = 15.sp,
                    modifier = Modifier.width(500.dp))
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
                        tabInfos[currentTab.value]!!.overrideClassValues()
                    }) { Text("Check & Generate") }
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
                        tabInfos[currentTab.value]!!.expandSortChoice.value = true
                    }) { Icon(Icons.Rounded.KeyboardArrowUp, "Sort") }
                    DropdownMenu(
                        expanded = tabInfos[currentTab.value]!!.expandSortChoice.value,
                        onDismissRequest = { tabInfos[currentTab.value]!!.expandSortChoice.value = false },
                        modifier = Modifier
                            .width(200.dp)
                            //.background(MaterialTheme.colors.surface)
                    ) {
                       currentTab.value.header.forEachIndexed { index, title ->
                            DropdownMenuItem(
                                onClick = {
                                    tabInfos[currentTab.value]!!.sortByField(index)
                                }) {
                                Text(text = title)
                            }
                        }
                    }
                }

                /*Column(verticalArrangement = Arrangement.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(300.dp)) {
                        TextField(value = tabInfos[currentTab.value]!!.exportFileName.value,
                            onValueChange = { tabInfos[currentTab.value]!!.exportFileName.value = it },
                            label = { Text("Export to file") },
                            modifier = Modifier.width(200.dp)
                        )
                        Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                            loadCSVToFile(currentTab, tabInfos[currentTab.value]!!.exportFileName.value)
                        }) { Icon(Icons.Rounded.ExitToApp, "Export") }
                    }

                    TextField(value = tabInfos[currentTab.value]!!.queryCSV.value,
                        onValueChange = {  },
                        modifier = Modifier
                            .height(500.dp)
                            .width(300.dp),
                        label = { Text("${currentTab.value.title} query output") }
                    )

                }*/
            }
        }
    }
}

fun loadCSVToFile(s: String, exportFileName: String) {
    csvWriter (). open(exportFileName) {
        val allString = s.split('\n').groupBy {it[0]}
        for ((name, key) in allString) {
            writeRow(name)
            for (elem in key) {
                writeRow(elem.subSequence(1..elem.length - 1).toList().joinToString(separator = ","))
            }
        }
    }
}

fun loadFromCSVFile(tab: MutableState<TabTypes>, importFileName: String): String {
    var currentName = ""
    val result = StringBuilder()
    csvReader (). open ( importFileName ) {
        readAllAsSequence (). forEach {row :  List < String > ->
            if (row[1].isEmpty()) {
                currentName = row.first()
            } else {
                result.appendLine(currentName + row.joinToString(prefix = ",", separator = ","))
            }
        }
    }
    return result.toString()
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Sport Management System",
        state = rememberWindowState(width = 800.dp, height = 720.dp)
    ) {
        myApplication(800.dp, 720.dp)
    }
}

/*
fun main(args: Array<String>) {
    for (key in args) {
        var it = 0
        var name = ""
        var cur:MutableList<StartEnrollSportsman> = mutableListOf()
        csvReader (). open ( key ) {
            readAllAsSequence (). forEach {row :  List < String > ->
                it += 1
                if (it == 1) {
                    name = row[0]
                } else {
                    cur.add(StartEnrollSportsman(row[0], row[1], row[2].toInt(), row[3], row[4]))
                }
            }
        }
        Group(name, cur)
    }
}
*/