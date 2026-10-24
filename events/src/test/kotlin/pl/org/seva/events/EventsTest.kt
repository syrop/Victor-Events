/*
 * Copyright (C) 2019 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you like this program, consider donating bitcoin: bc1qncxh5xs6erq6w4qz3a7xl7f50agrgn3w58dsfp
 */

package pl.org.seva.events

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.comm.CommsDao
import pl.org.seva.events.event.Event
import pl.org.seva.events.event.Events
import pl.org.seva.events.event.EventsDao
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.ui.ColorFactory
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class EventsTest {

    private lateinit var fsReader: FsReader
    private lateinit var fsWriter: FsWriter
    private lateinit var eventsDao: EventsDao
    private lateinit var events: Events

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fsReader = mock(FsReader::class.java)
        fsWriter = mock(FsWriter::class.java)
        eventsDao = mock(EventsDao::class.java)
        events = Events(fsReader, fsWriter, eventsDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAdd() = runTest(UnconfinedTestDispatcher()) {
        val event = Event(comm = "Alpha", name = "Meetup")
        var updateCount = 0
        val job = launch { events.updates.collect { updateCount++ } }

        assertTrue(events.isEmpty)
        events.add(event)
        assertFalse(events.isEmpty)
        assertEquals(1, events.size)
        assertEquals(event, events[0])
        verify(fsWriter).addAll(event)
        verify(eventsDao) addAll listOf(event)
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testAddFrom() = runTest(UnconfinedTestDispatcher()) {
        val comms = Comms(fsReader, fsWriter, mock(CommsDao::class.java), events, mock(ColorFactory::class.java))
        val comm = Comm(name = "KotlinUG", comms = comms)
        val remoteEvent = Event(comm = "KotlinUG", name = "Hackathon")
        `when`(fsReader readEventsFrom "kotlinug").thenReturn(listOf(remoteEvent))

        events addFrom comm
        assertEquals(1, events.size)
        assertEquals(remoteEvent, events[0])
        verify(fsWriter).addAll(remoteEvent)
        verify(eventsDao) addAll listOf(remoteEvent)
    }

    @Test
    fun testDeleteLocallyFrom() = runTest(UnconfinedTestDispatcher()) {
        val comms = Comms(fsReader, fsWriter, mock(CommsDao::class.java), events, mock(ColorFactory::class.java))
        val commAlpha = Comm(name = "Alpha", comms = comms)
        val eventAlpha = Event(comm = "Alpha", name = "Alpha Event")
        val eventBeta = Event(comm = "Beta", name = "Beta Event")
        events.add(eventAlpha, eventBeta)

        var updateCount = 0
        val job = launch { events.updates.collect { updateCount++ } }

        events deleteLocallyFrom commAlpha
        assertEquals(1, events.size)
        assertEquals(eventBeta, events[0])
        verify(eventsDao) delete eventAlpha
        verify(eventsDao, never()) delete eventBeta
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testLoadFromDbSortsByTime() = runTest(UnconfinedTestDispatcher()) {
        val later = Event(comm = "Alpha", name = "Later", time = LocalDateTime.of(2026, 5, 20, 18, 0))
        val earlier = Event(comm = "Alpha", name = "Earlier", time = LocalDateTime.of(2026, 5, 10, 12, 0))
        `when`(eventsDao.getAll()).thenReturn(listOf(Event.Entity(later), Event.Entity(earlier)))

        var updateCount = 0
        val job = launch { events.updates.collect { updateCount++ } }

        events.loadFromDb()
        assertEquals(2, events.size)
        assertEquals(earlier, events[0])
        assertEquals(later, events[1])
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testFromComms() = runTest {
        val comms = Comms(fsReader, fsWriter, mock(CommsDao::class.java), mock(Events::class.java), mock(ColorFactory::class.java))
        val comm = Comm(name = "Alpha", comms = comms)
        comms join comm

        val event = Event(comm = "Alpha", name = "Conference")
        `when`(fsReader readEventsFrom "alpha").thenReturn(listOf(event))

        val loaded = events fromComms comms
        assertEquals(listOf(event), loaded)
        assertEquals(1, events.size)
        assertEquals(event, events[0])
        verify(eventsDao).clear()
        verify(eventsDao) add event
    }

    @Test
    fun testEventEntityConversion() {
        val time = LocalDateTime.of(2026, 6, 15, 19, 30)
        val event = Event(
                comm = "Alpha",
                name = "Workshop",
                time = time,
                location = LatLng(51.1079, 17.0385),
                address = "Wrocław",
                desc = "Kotlin Workshop",
        )
        val restored = Event.Entity(event).value()
        assertEquals(event, restored)
    }
}
