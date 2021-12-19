package ru.emkn.kotlin.sms

import androidx.compose.runtime.MutableState


enum class TabTypes(val title: String, val header: List<String>) {
    TEAMS("Teams", listOf("Team", "Surname", "Name", "BirthYear", "Category", "Group")), // EnrollSportsman
    GROUPS("Groups", listOf("Group", "Number", "Surname", "Name", "BirthYear", "Category", "Collective", "StartTime")), // StartSportsman
    DIST("Distances", listOf("Group", "CheckPointName")), // Station
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
    initClassLines: MutableState<List<T>>,
    classLines: MutableState<List<T>>,
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
    var initClassLines: MutableState<List<T>>
    var classLines: MutableState<List<T>>
    val whichFilter: MutableState<String>
    val classConstructor: (List<String>) -> T
    var openDialog: MutableState<Boolean>
    var dialogReport: MutableState<Report>
    init {
        this.initCSV = initCSV
        this.plainCSV = plainCSV
        this.classLines = classLines
        this.expandSortChoice = expandSortChoice
        this.expandSortDescChoice = expandSortDescChoice
        this.expandFilterChoice = expandFilterChoice
        this.exportFileName = exportFileName
        this.importFileName = importFileName
        this.state = state
        this.warning = warning
        this.initClassLines = initClassLines
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
                return Report(States.WRONG, "Wrong format in line ${index + 1}, can't process")
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

        classLines.value = table.map { classConstructor(it) } // No exceptions guaranteed
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
        plainCSV.value = classLines.value.joinToString("\n") { it.toString() }
    }

    fun invokeDialog(report: Report) {
        dialogReport.value = report
        openDialog.value = true
    }
}