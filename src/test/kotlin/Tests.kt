package ru.emkn.kotlin.sms
import kotlin.reflect.full.*
import kotlin.test.*

internal class Tests {

    @Test
    fun checkTime() {
        val time1: String = "12:59:00"
        val time2: String = "23:00:59"
        var Time1: Time = Time(time1)
        var Time2: Time = Time(time2)
        assertEquals(time1, Time1.toString())
        assertEquals(time2, Time2.toString())
        assert(Time1 < Time2)
        assert(Time2 > Time1)
        assertEquals("10:01:59", (Time2 - Time1).toString())
        assertEquals("23:01:00", (Time2++).toString())
    }

}
