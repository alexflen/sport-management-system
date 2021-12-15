package ru.emkn.kotlin.sms

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

enum class TabTypes(val title: String) {
    GROUPS("Groups"),
    DIST("Distances"),
    TEAMS("Teams"),
    PARTICIPANTS("Participants"),
    MARKS("Marks on check points"),
    RESULTS("Results")
}

enum class ColumnTypes {
    INT, STRING, DOUBLE
}

enum class States {
    OK, WRONG, EMPTY, ONLYHEADER
}

data class Report(val state: States, val message: String = "")

class TabInfo(var queryCSV: MutableState<String>,
              var plainCSV: MutableState<String>,
              var importFileName: MutableState<String>,
              var exportFileName: MutableState<String>,
              var warning: MutableState<String>) {
    var state = States.EMPTY
    var queryLines = listOf<String>()
    var csvLines = listOf<List<String>>()
        set(value) {
            field = value
            // TODO something about empty
            val report = checkIfOkCSV(value)
            state = report.state
            warning.value = report.message
            if (report.state == States.OK) {
                updateColumnTypes()
            }
        }

    var columnTypes = listOf<ColumnTypes>()

    private fun updatePlainCSV() {
        plainCSV.value = csvLines.joinToString("\n") { it.joinToString(",") }
    }

    private fun updateQueryCSV() {
        queryCSV.value = queryLines.joinToString("\n")
    }

    private fun updateColumnTypes() {
        require(state == States.OK)
        val tempColumnTypes = MutableList(csvLines[0].size) { ColumnTypes.STRING }
        for (column in csvLines[0].indices) {
            var type = ColumnTypes.INT
            csvLines.forEach { row ->
                if (type == ColumnTypes.INT) {
                    if (row[column].toIntOrNull() == null) {
                        type = ColumnTypes.DOUBLE
                    }
                }

                if (type == ColumnTypes.DOUBLE) {
                    if (row[column].toDoubleOrNull() == null) {
                        type = ColumnTypes.STRING
                    }
                }
            }
            tempColumnTypes[column] = type
        }
        columnTypes = tempColumnTypes.toList()
    }

    fun checkIfOkCSV(rows: List<List<String>>): Report {
        // Check if all have the same number of columns
        if (rows.isEmpty()) return Report(States.EMPTY, "Warning: empty CSV")
        if (rows.size == 1) return Report(States.ONLYHEADER, "Warning: only header in CSV")
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
        csvLines = text.split("\n").map { it.split(',') }
        println(csvLines.size)
        plainCSV.value = text
    }

    fun sortBy(column: String) {
        val index = csvLines[0].indexOf(column)
        val columnType = columnTypes[index]
        require(index != -1)

        queryLines = when (columnType) {
            ColumnTypes.INT -> csvLines.sortedBy { it[index].toInt() }
            ColumnTypes.DOUBLE -> csvLines.sortedBy { it[index].toDouble() }
            ColumnTypes.STRING -> csvLines.sortedBy { it[index] }
        }.map { it.joinToString(",") }

        updateQueryCSV()
    }
}

@Composable
@Preview
fun myApplication() {
    MaterialTheme {
        val currentTab = remember { mutableStateOf(TabTypes.GROUPS) }
        val tabInfos = TabTypes.values().associateWith { TabInfo(remember { mutableStateOf("") },
            remember { mutableStateOf("") }, remember { mutableStateOf("") },
        remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") }) }

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
                        modifier = Modifier.width(300.dp)
                    ) {
                        TextField(
                            value = tabInfos[currentTab.value]!!.importFileName.value,
                            onValueChange = { tabInfos[currentTab.value]!!.importFileName.value = it },
                            label = { Text("Import from file") },
                            modifier = Modifier.width(200.dp)
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
                            .width(300.dp),
                        label = { Text("${currentTab.value.title} csv info") })
                    Text(tabInfos[currentTab.value]!!.warning.value, color = Color.Red, fontSize = 15.sp,
                    modifier = Modifier.width(300.dp))
                }

                Column(verticalArrangement = Arrangement.Top) {
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

                }
            }
        }
    }
}

fun loadCSVToFile(tab: MutableState<TabTypes>, exportFileName: Any) {
    TODO()
}

fun loadFromCSVFile(tab: MutableState<TabTypes>, importFileName: String): String {
    TODO()
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Sport Management System",
        state = rememberWindowState(width = 800.dp, height = 720.dp)
    ) {
        myApplication()
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