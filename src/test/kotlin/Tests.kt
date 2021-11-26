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
        assertEquals(1.59361, Time2 / Time1, 1e-3)
        try {
            Time1 = Time("24:00:00")
        } catch (e: IllegalStateException) {
            assertEquals(e.message, "Incorrect H in time")
        }
        try {
            Time1 - Time2
        } catch (e: IllegalStateException) {
            assertEquals(e.message, "More is subtracted from less time")
        }
    }

}
