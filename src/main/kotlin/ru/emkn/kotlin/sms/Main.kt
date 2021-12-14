package ru.emkn.kotlin.sms

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
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

@Composable
@Preview
fun myApplication() {
    MaterialTheme {
        class TabInfo(var plainText: MutableState<String>,
                      var plainCSV: MutableState<String>) {
            var textLines = listOf<String>()
            var csvLines = listOf<String>()

            fun updatePlainText() {
                plainText.value = textLines.joinToString("\n")
            }

            fun updatePlainCSV() {
                plainCSV.value =  csvLines.joinToString("\n")
            }

            private fun makeCSVFromText() {
                csvLines = textLines.map { it.split("""\s+""".toRegex()).joinToString(",") }
                updatePlainCSV()
            }

            private fun makeTextFromCSV() {
                textLines = csvLines.map { it.split(""",+""".toRegex()).joinToString(" ") }
                updatePlainText()
            }

            fun updateWhenText(text: String) {
                textLines = text.split("\n")
                updatePlainText()
                makeCSVFromText()
            }

            fun updateWhenCSV(text: String) {
                csvLines = text.split("\n")
                updatePlainCSV()
                makeTextFromCSV()
            }
        }

        val currentTab = remember { mutableStateOf(TabTypes.GROUPS) }
        val tabInfos = TabTypes.values().associateWith { TabInfo(remember { mutableStateOf("") }, remember { mutableStateOf("") }) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                TabTypes.values().forEach { iterated ->
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                        currentTab.value = iterated
                    }) { Text(iterated.title) }
                }
            }
            Text(currentTab.value.title, color = Color.Magenta, fontSize = 25.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()) {
                TextField(value = tabInfos[currentTab.value]!!.plainText.value,
                    onValueChange = { tabInfos[currentTab.value]!!.updateWhenText(it) },
                    label = { Text("${currentTab.value.title} text info") })
                TextField(value = tabInfos[currentTab.value]!!.plainCSV.value,
                    onValueChange = { tabInfos[currentTab.value]!!.updateWhenCSV(it) },
                    enabled = false,
                    label = { Text("${currentTab.value.title} csv info") })
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Sport Management System",
        state = rememberWindowState(width = 800.dp, height = 700.dp)
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