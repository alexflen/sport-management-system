package ru.emkn.kotlin.sms

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

class TabInfo<T>(
              initCSV: MutableState<String>,
              plainCSV: MutableState<String>,
                importFileName: MutableState<String>,
                exportFileName: MutableState<String>,
              warning: MutableState<String>,
                expandSortChoice: MutableState<Boolean>,
                expandSortDescChoice: MutableState<Boolean>,
                expandFilterChoice: MutableState<Boolean>,
              state: MutableState<States>,
                initLines: MutableState<List<T>>,
                csvLines: MutableState<List<T>>,
              whichFilter: MutableState<String>,
                openDialog: MutableState<Boolean>,
                dialogReport: MutableState<Report>,
              classConstructor: (List<String>) -> T) {
    var initCSV: MutableState<String>
    var plainCSV: MutableState<String>
    var importFileName: MutableState<String>
    var exportFileName: MutableState<String>
    var warning: MutableState<String>
    var expandSortChoice: MutableState<Boolean>
    var expandSortDescChoice: MutableState<Boolean>
    var expandFilterChoice: MutableState<Boolean>
    var state: MutableState<States>
    var initLines: MutableState<List<T>>
    var csvLines: MutableState<List<T>>
    val whichFilter: MutableState<String>
    val classConstructor: (List<String>) -> T
    var openDialog: MutableState<Boolean>
    var dialogReport: MutableState<Report>
    init {
        this.initCSV = initCSV
        this.plainCSV = plainCSV
        this.csvLines = csvLines
        this.expandSortChoice = expandSortChoice
        this.expandSortDescChoice = expandSortDescChoice
        this.expandFilterChoice = expandFilterChoice
        this.exportFileName = exportFileName
        this.importFileName = importFileName
        this.state = state
        this.warning = warning
        this.initLines = initLines
        this.classConstructor = classConstructor
        this.whichFilter = whichFilter
        this.openDialog = openDialog
        this.dialogReport = dialogReport
    }

    private fun checkIfOkCSV(rows: List<List<String>>): Report {
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

    private fun checkIfOkClasses(rows: List<List<String>>): Report {
        rows.forEachIndexed { index, it ->
            try { classConstructor(it) }
            catch (e : Exception) {
                return Report(States.WRONG, "Wrong format in line $index, can't process")
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
        innerRequire(state.value == States.OK, "Fix the mistakes in CSV first")
        val table = plainCSV.value.split("\n").map { it.split(',') }

        val report = checkIfOkClasses(table)
        if (report.state != States.OK) {
            invokeDialog(report)
            return report
        }

        csvLines.value = table.map { classConstructor(it) } // No exceptions guaranteed
        return Report(States.OK)
    }

    private fun getTextLines(): List<List<String>> {
        return plainCSV.value.split("\n").map { it.split(",") }
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

    private fun innerRequire(result: Boolean, message: String) {
        if (!result)
            invokeDialog(Report(States.WRONG, message))
    }

    fun sortByField(index: Int) {
        val table = getTextLines()
        if (table.isNotEmpty())
            innerRequire(index >= 0 && index < table[0].size, "BUG: Index of a field out of range")
        val columnType = getColumnType(table, index)
        plainCSV.value = sortTableBy(table, index, columnType).joinToString("\n") { it.joinToString(",") }
    }

    fun sortDescByField(index: Int) {
        val table = getTextLines()
        if (table.isNotEmpty())
            innerRequire(index >= 0 && index < table[0].size, "BUG: Index of a field out of range")
        val columnType = getColumnType(table, index)
        plainCSV.value = sortTableByDescending(table, index, columnType).joinToString("\n") { it.joinToString(",") }
    }

    fun filterByField(index: Int) {
        val table = getTextLines()
        if (table.isNotEmpty())
            innerRequire(index >= 0 && index < table[0].size, "BUG: Index of a field out of range")
        plainCSV.value = filterTableBy(table, index, whichFilter.value).joinToString("\n") { it.joinToString(",") }
    }

    fun turnBackToWorking() {
        plainCSV.value = csvLines.value.joinToString("\n") { it.toString() }
    }

    fun invokeDialog(report: Report) {
        dialogReport.value = report
        openDialog.value = true
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun myApplication(width: Dp, height: Dp) {
    val textWidth = width.div(1.4F)
    val butWidth = width - textWidth - 100.dp
    MaterialTheme {
        val currentTab = remember { mutableStateOf(TabTypes.TEAMS) }
        val tabInfos = mapOf( TabTypes.TEAMS to TabInfo<EnrollSportsman>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(false) },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }, remember { mutableStateOf("") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(Report(States.OK)) }) { EnrollSportsman(it) },
            TabTypes.GROUPS to TabInfo<StartSportsman>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(false) },  remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }, remember { mutableStateOf("") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(Report(States.OK)) }) { StartSportsman(it) },
            TabTypes.DIST to TabInfo<Station>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }, remember { mutableStateOf("") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(Report(States.OK)) }) { Station(it) },
            TabTypes.MARKS to TabInfo<StationPerformance>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }, remember { mutableStateOf("") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(Report(States.OK)) }) { StationPerformance(it) },
            TabTypes.RESULTS to TabInfo<ResultSportsman>(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }, remember { mutableStateOf("") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(Report(States.OK)) }) { ResultSportsman(it) },
            )

        if (tabInfos[currentTab.value]!!.openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    if (tabInfos[currentTab.value]!!.dialogReport.value.state == States.OK)
                         tabInfos[currentTab.value]!!.openDialog.value = false
                },
                title = {
                    if (tabInfos[currentTab.value]!!.dialogReport.value.state == States.OK)
                        Text(text = "Message")
                    else
                        Text(text = "Error")
                },
                text = {
                    if (tabInfos[currentTab.value]!!.dialogReport.value.state == States.OK)
                        Text(tabInfos[currentTab.value]!!.dialogReport.value.message)
                    else
                        Text(text = tabInfos[currentTab.value]!!.dialogReport.value.message,
                            color = Color.Red, fontSize = 18.sp)
                },
                confirmButton = { Button(onClick = { tabInfos[currentTab.value]!!.openDialog.value = false })
                            { Text("OK") }
                },
                modifier = Modifier.width(width.div(3))
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.width(textWidth)
                    ) {
                        OutlinedTextField(
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
                    OutlinedTextField(value = currentTab.value.header.joinToString(","),
                        onValueChange = {  },
                        modifier = Modifier
                            .width(textWidth),
                        label = { Text("${currentTab.value.title} header") })
                    OutlinedTextField(value = tabInfos[currentTab.value]!!.plainCSV.value,
                        onValueChange = { tabInfos[currentTab.value]!!.updateWhenCSV(it) },
                        modifier = Modifier
                            .height(400.dp)
                            .width(textWidth),
                        label = { Text("${currentTab.value.title} csv info") })

                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(textWidth)
                    ) {
                        OutlinedTextField(
                            value = tabInfos[currentTab.value]!!.exportFileName.value,
                            onValueChange = { tabInfos[currentTab.value]!!.exportFileName.value = it },
                            label = { Text("Export to file") },
                            modifier = Modifier.width(400.dp)
                        )
                        Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                            loadCSVToFile(tabInfos[currentTab.value]!!.plainCSV.value,
                                tabInfos[currentTab.value]!!.exportFileName.value)
                        }) { Icon(Icons.Rounded.ExitToApp, "Import") }
                    }

                    Text(tabInfos[currentTab.value]!!.warning.value, color = Color.Red, fontSize = 18.sp,
                        modifier = Modifier.width(textWidth).padding(5.dp))
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        tabInfos[currentTab.value]!!.overrideClassValues()
                    }) { Text("Check & Generate") }
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        tabInfos[currentTab.value]!!.expandSortChoice.value = true
                    }) { Icon(Icons.Rounded.KeyboardArrowDown, "Sort") }
                    DropdownMenu(
                        expanded = tabInfos[currentTab.value]!!.expandSortChoice.value,
                        onDismissRequest = { tabInfos[currentTab.value]!!.expandSortChoice.value = false },
                        modifier = Modifier
                            .width(butWidth)
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

                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        tabInfos[currentTab.value]!!.expandSortDescChoice.value = true
                    }) { Icon(Icons.Rounded.KeyboardArrowUp, "SortDesc") }
                    DropdownMenu(
                        expanded = tabInfos[currentTab.value]!!.expandSortDescChoice.value,
                        onDismissRequest = { tabInfos[currentTab.value]!!.expandSortDescChoice.value = false },
                        modifier = Modifier
                            .width(butWidth)
                        //.background(MaterialTheme.colors.surface)
                    ) {
                        currentTab.value.header.forEachIndexed { index, title ->
                            DropdownMenuItem(
                                onClick = {
                                    tabInfos[currentTab.value]!!.sortDescByField(index)
                                }) {
                                Text(text = title)
                            }
                        }
                    }
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        tabInfos[currentTab.value]!!.expandFilterChoice.value = true
                    }) { Icon(Icons.Rounded.Search, "FilterBy") }
                    DropdownMenu(
                        expanded = tabInfos[currentTab.value]!!.expandFilterChoice.value,
                        onDismissRequest = { tabInfos[currentTab.value]!!.expandFilterChoice.value = false },
                        modifier = Modifier
                            .width(butWidth)
                        //.background(MaterialTheme.colors.surface)
                    ) {
                        currentTab.value.header.forEachIndexed { index, title ->
                            DropdownMenuItem(
                                onClick = {
                                    tabInfos[currentTab.value]!!.filterByField(index)
                                }) {
                                Text(text = title)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = tabInfos[currentTab.value]!!.whichFilter.value,
                        onValueChange = { tabInfos[currentTab.value]!!.whichFilter.value = it },
                        label = { Text("Filter") },
                        textStyle = TextStyle(color = Color.Blue),
                        modifier = Modifier.width(butWidth)
                    )

                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        tabInfos[currentTab.value]!!.turnBackToWorking()
                    }) { Icon(Icons.Rounded.Clear, "ReturnToWorking") }
                }
            }
        }
    }
}

fun loadCSVToFile(s: String, exportFileName: String): Report {
    var answer = Report(States.OK)
    csvWriter (). open(exportFileName) {
        val allString = s.split('\n').groupBy {it.split(',')[0]}
        for ((name, key) in allString) {
            writeRow(name)
            for (elem in key) {
                val position = elem.firstOrNull {it == ','}
                if (position == null) {
                    answer = Report(States.WRONG, "$elem not have ,")
                } else {
                    writeRow(elem.subSequence(position.toInt() + 1 until elem.length))
                }
            }
        }
    }
    return answer
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

fun transformationToStartSportsman(a: List<EnrollSportsman>): List<StartSportsman> {
    return AllStartGroups(a).groups.flatMap {it.participants}
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

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Sport Management System",
        state = rememberWindowState(width = 1000.dp, height = 750.dp)
    ) {
        myApplication(1000.dp, 750.dp)
    }
}