package ru.emkn.kotlin.sms
import kotlin.reflect.full.*
import kotlin.test.*

internal class Tests {

    @Test
    fun checkTime() {
        val time1: String = "12:59:00"
        val time2: String = "23:00:59"
        val Time1: Time = Time(time1)
        val Time2: Time = Time(time2)
        assertEquals(time1, Time1.toString())
        assertEquals(time2, Time2.toString())
    }

}
