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

    @Test
    fun checkSportsMan() {
        val cur = Sportsman("shilyaeva", "ekaterina", 2003, "egoi", "ege")
        assertEquals("shilyaeva,ekaterina,2003,ege", cur.toString())
        val now = Sportsman("karpovich", "evgeny", 2003, "hockey")
        assertEquals("karpovich,evgeny,2003,", now.toString())
    }

    @Test
    fun checkStartEnrollSportsman() {
        val cur = StartEnrollSportsman("shilyaeva", "ekaterina", 2003, "egoi", "oge", 123)
        assertEquals("shilyaeva,ekaterina,2003,,oge,egoi", cur.toStringEnroll())
        cur.start = Time("12:12:12")
        assertEquals("123,shilyaeva,ekaterina,2003,,12:12:12", cur.toStringStart())
        val now = StartEnrollSportsman("karpovich", "evgeny", 2003, "hockey", "icpc")
        assertEquals("karpovich,evgeny,2003,,icpc,hockey", now.toStringEnroll())
    }

    @Test
    fun checkStation() {
        val cur = Station("football", 123, Time("00:00:00"))
        assertEquals("football,00:00:00", cur.toStringName())
        assertEquals("123,00:00:00", cur.toStringNumber())
    }

    @Test
    fun checkStationSportsman() {
        val cur: List<Station> = listOf(Station("football", 123, Time("00:00:00")), Station("hockey", 312, Time("00:10:00")), Station("basketball", 231, Time("01:00:00")))
        val now = StationSportsman(123, cur)
        assertEquals("123\nfootball,00:00:00\nhockey,00:10:00\nbasketball,01:00:00\n", now.toString())
        try {
            val tmp: List<Station> = listOf(Station("football", 666, Time("23:59:59")), Station("hockey", 111, Time("11:11:11")))
        } catch (e: IllegalStateException) {
            assertEquals(e.message, "Incorrect time for the passage of the station by the athlete with the number 123")
        }
    }

    @Test
    fun checkGroup() {
        val cur: List<StartEnrollSportsman> = listOf(StartEnrollSportsman("karpovich", "evgeny", 2003, "hockey", "icpc"), StartEnrollSportsman("shilyaeva", "ekaterina", 2003, "egoi", "oge", 123))
        cur[0].start = Time("12:34:56")
        cur[1].start = Time("21:00:59")
        val now = Group("traktor", cur)
        assertEquals("traktor\n,karpovich,evgeny,2003,,12:34:56\n123,shilyaeva,ekaterina,2003,,21:00:59\n", now.toStringStart())
    }
}
