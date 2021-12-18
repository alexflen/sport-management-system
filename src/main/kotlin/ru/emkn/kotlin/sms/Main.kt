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

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun myApplication(width: Dp, height: Dp) {
    val textWidth = width.div(1.4F)
    val butWidth = width - textWidth - 100.dp
    MaterialTheme {
        val currentTabType = remember { mutableStateOf(TabTypes.TEAMS) }
        val constructorsToBuild = mapOf<TabTypes, (List<String>) -> Any>(TabTypes.TEAMS to { EnrollSportsman(it) },
                    TabTypes.GROUPS to { StartSportsman(it) }, TabTypes.DIST to { Station(it) },
                    TabTypes.MARKS to { StationPerformance(it) }, TabTypes.RESULTS to { ResultSportsman(it) })
        val tabInfos = TabTypes.values().associateWith { tab ->
            TabInfo(remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("") },
                remember { mutableStateOf("") }, remember { mutableStateOf("Warning: empty CSV") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(false) },
                remember { mutableStateOf(false) }, remember { mutableStateOf(States.EMPTY) },
                remember { mutableStateOf(listOf()) },
                remember { mutableStateOf(listOf()) }, remember { mutableStateOf("") },
                remember { mutableStateOf(false) }, remember { mutableStateOf(Report(States.OK)) })
            { constructorsToBuild[tab]!!(it) }
        }
        
        val currentTab = tabInfos[currentTabType.value]!!
        // Message and Error window
        if (currentTab.openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    if (currentTab.dialogReport.value.state == States.OK)
                         currentTab.openDialog.value = false
                },
                title = {
                    if (currentTab.dialogReport.value.state == States.OK)
                        Text(text = "Message")
                    else
                        Text(text = "Error")
                },
                text = {
                    if (currentTab.dialogReport.value.state == States.OK)
                        Text(currentTab.dialogReport.value.message)
                    else
                        Text(text = currentTab.dialogReport.value.message,
                            color = Color.Red, fontSize = 18.sp)
                },
                confirmButton = { Button(onClick = { currentTab.openDialog.value = false })
                            { Text("OK") }
                },
                modifier = Modifier.width(width.div(3))
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // Tab list header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                TabTypes.values().forEach { iterated ->
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                        currentTabType.value = iterated
                    }, colors = if (currentTabType.value == iterated) ButtonDefaults.buttonColors(Color(0xffffb954))
                    else ButtonDefaults.buttonColors(Color.Unspecified)) { Text(iterated.title) }
                }
            }

            // Tab title
            Text(currentTabType.value.title, color = Color.Blue, fontSize = 25.sp)
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.Top) {
                    //Import from file
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(textWidth)
                    ) {
                        OutlinedTextField(
                            value = currentTab.importFileName.value,
                            onValueChange = { currentTab.importFileName.value = it },
                            label = { Text("Import from file") },
                            modifier = Modifier.width(400.dp)
                        )
                        Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                            try {
                                currentTab.updateWhenCSV(loadFromCSVFile(currentTab.importFileName.value))
                                currentTab.overrideClassValues()
                            }
                            catch(e: Exception) {
                                currentTab.invokeDialog(Report(States.WRONG, "Failed to read the file"))
                            }
                        }) { Icon(Icons.Rounded.Add, "Import") }
                    }

                    //CSV data
                    OutlinedTextField(value = currentTabType.value.header.joinToString(","),
                        onValueChange = {  },
                        modifier = Modifier
                            .width(textWidth),
                        label = { Text("${currentTabType.value.title} header") })
                    OutlinedTextField(value = currentTab.plainCSV.value,
                        onValueChange = { currentTab.updateWhenCSV(it) },
                        modifier = Modifier
                            .height(400.dp)
                            .width(textWidth),
                        label = { Text("${currentTabType.value.title} csv info") })

                    // Export to file
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(textWidth)
                    ) {
                        OutlinedTextField(
                            value = currentTab.exportFileName.value,
                            onValueChange = { currentTab.exportFileName.value = it },
                            label = { Text("Export to file") },
                            modifier = Modifier.width(400.dp)
                        )
                        Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                            loadCSVToFile(currentTab.plainCSV.value,
                                currentTab.exportFileName.value)
                            currentTab.invokeDialog(Report(States.OK, "Successfully exported"))
                        }) { Icon(Icons.Rounded.ExitToApp, "Import") }
                    }

                    // Warning text
                    Text(currentTab.warning.value, color = Color.Red, fontSize = 18.sp,
                        modifier = Modifier.width(textWidth).padding(5.dp))
                }

                // Buttons
                Column(verticalArrangement = Arrangement.Center) {
                    // Check and generate
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        val report = currentTab.overrideClassValues()
                        if (report.state == States.OK) {
                            when {
                                currentTabType.value == TabTypes.TEAMS ->
                                {
                                    tabInfos[TabTypes.GROUPS]!!.updateWhenCSV(
                                        transformationToStartSportsman(
                                            tabInfos[TabTypes.TEAMS]!!.csvLines.value
                                            as List<EnrollSportsman>).joinToString("\n") { it.toString() })
                                    tabInfos[TabTypes.TEAMS]!!.invokeDialog(Report(States.OK, "Successfully generated GROUPS"))
                                }
                                currentTabType.value == TabTypes.GROUPS ||
                                    currentTabType.value == TabTypes.MARKS ||
                                        currentTabType.value == TabTypes.DIST ->
                                {
                                    val reportCor = checkCorrelation(tabInfos[TabTypes.GROUPS]!!.csvLines.value as List<StartSportsman>,
                                        tabInfos[TabTypes.DIST]!!.csvLines.value as List<Station>,
                                        tabInfos[TabTypes.MARKS]!!.csvLines.value as List<StationPerformance>)
                                    if (reportCor.state != States.OK) {
                                        currentTab.invokeDialog(reportCor)
                                    } else {
                                        tabInfos[TabTypes.RESULTS]!!.updateWhenCSV(
                                            generateResults(tabInfos[TabTypes.GROUPS]!!.csvLines.value as List<StartSportsman>,
                                            tabInfos[TabTypes.MARKS]!!.csvLines.value
                                            as List<StationPerformance>).joinToString("\n") { it.toString() }
                                        )
                                        currentTab.invokeDialog(Report(States.OK, "Successfully updated RESULTS"))
                                    }
                                }
                            }
                        }
                    }) { Text("Check & Generate") }

                    // Sort
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        currentTab.expandSortChoice.value = true
                    }) { Icon(Icons.Rounded.KeyboardArrowDown, "Sort") }
                    DropdownMenu(
                        expanded = currentTab.expandSortChoice.value,
                        onDismissRequest = { currentTab.expandSortChoice.value = false },
                        modifier = Modifier
                            .width(butWidth)
                            //.background(MaterialTheme.colors.surface)
                    ) {
                       currentTabType.value.header.forEachIndexed { index, title ->
                            DropdownMenuItem(
                                onClick = {
                                    currentTab.sortByField(index)
                                }) {
                                Text(text = title)
                            }
                        }
                    }

                    // SortDescending
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        currentTab.expandSortDescChoice.value = true
                    }) { Icon(Icons.Rounded.KeyboardArrowUp, "SortDesc") }
                    DropdownMenu(
                        expanded = currentTab.expandSortDescChoice.value,
                        onDismissRequest = { currentTab.expandSortDescChoice.value = false },
                        modifier = Modifier
                            .width(butWidth)
                        //.background(MaterialTheme.colors.surface)
                    ) {
                        currentTabType.value.header.forEachIndexed { index, title ->
                            DropdownMenuItem(
                                onClick = {
                                    currentTab.sortDescByField(index)
                                }) {
                                Text(text = title)
                            }
                        }
                    }

                    // Filter
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        currentTab.expandFilterChoice.value = true
                    }) { Icon(Icons.Rounded.Search, "FilterBy") }
                    DropdownMenu(
                        expanded = currentTab.expandFilterChoice.value,
                        onDismissRequest = { currentTab.expandFilterChoice.value = false },
                        modifier = Modifier
                            .width(butWidth)
                        //.background(MaterialTheme.colors.surface)
                    ) {
                        currentTabType.value.header.forEachIndexed { index, title ->
                            DropdownMenuItem(
                                onClick = {
                                    currentTab.filterByField(index)
                                }) {
                                Text(text = title)
                            }
                        }
                    }

                    // Text for filter
                    OutlinedTextField(
                        value = currentTab.whichFilter.value,
                        onValueChange = { currentTab.whichFilter.value = it },
                        label = { Text("Filter") },
                        textStyle = TextStyle(color = Color.Blue),
                        modifier = Modifier.width(butWidth)
                    )

                    // Clear filters and sort (if not checked)
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).width(butWidth), onClick = {
                        currentTab.turnBackToWorking()
                    }) { Icon(Icons.Rounded.Clear, "ReturnToWorking") }
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Sport Management System",
        state = rememberWindowState(width = 1000.dp, height = 750.dp)
    ) {
        myApplication(1000.dp, 750.dp)
    }
}