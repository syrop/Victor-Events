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

import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.CommViewModel
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.comm.CommsDao
import pl.org.seva.events.event.Event
import pl.org.seva.events.event.EventCreateViewModel
import pl.org.seva.events.event.EventLocation
import pl.org.seva.events.event.EventViewModel
import pl.org.seva.events.event.Events
import pl.org.seva.events.event.EventsDao
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.init.kodein
import pl.org.seva.events.main.ui.ColorFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private lateinit var fsReader: FsReader
    private lateinit var comms: Comms
    private lateinit var events: Events

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fsReader = mock(FsReader::class.java)
        val fsWriter = mock(FsWriter::class.java)
        val commsDao = mock(CommsDao::class.java)
        val eventsDao = mock(EventsDao::class.java)
        val colorFactory = mock(ColorFactory::class.java)
        events = Events(fsReader, fsWriter, eventsDao)
        comms = Comms(fsReader, fsWriter, commsDao, events, colorFactory)
        kodein = DI {
            bindInstance { comms }
            bindInstance { events }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testEventCreateViewModelIsFilledInAndClear() {
        val viewModel = EventCreateViewModel()
        assertFalse(viewModel.isFilledIn)

        viewModel.name.value = "Meetup"
        assertTrue(viewModel.isFilledIn)

        viewModel.time.value = LocalTime.of(18, 30)
        viewModel.date.value = LocalDate.of(2026, 10, 24)
        viewModel.location.value = EventLocation(LatLng(51.1, 17.0), "Wrocław")
        viewModel.desc.value = "Monthly meetup"
        assertTrue(viewModel.isFilledIn)

        viewModel.clear()
        assertFalse(viewModel.isFilledIn)
        assertNull(viewModel.name.value)
        assertNull(viewModel.time.value)
        assertNull(viewModel.date.value)
        assertNull(viewModel.location.value)
        assertNull(viewModel.desc.value)
    }

    @Test
    fun testCommViewModelWithPositionAndUpdate() = runTest {
        `when`(fsReader readEventsFrom "developers").thenReturn(emptyList())
        val comm = Comm(name = "Developers", desc = "Dev community", isAdmin = true, comms = comms)
        comms join comm

        val state = SavedStateHandle()
        val viewModel = CommViewModel(state)
        assertEquals(Comm.DUMMY, viewModel.comm)

        viewModel.withPosition(0)
        assertEquals(comm, viewModel.comm)
        assertEquals(0, state.get<Int>(CommViewModel.COMM_POSITION))
        assertEquals("Developers", viewModel.name.value)
        assertEquals("Dev community", viewModel.desc.value)
        assertEquals(true, viewModel.isAdmin.value)

        viewModel.desc.value = "Modified locally"
        viewModel.reset()
        assertEquals("Dev community", viewModel.desc.value)

        val updatedComm = comm.copy(desc = "Updated description", isAdmin = false)
        comms update updatedComm
        viewModel.update()
        assertEquals("Updated description", viewModel.desc.value)
        assertEquals(false, viewModel.isAdmin.value)
    }

    @Test
    fun testEventViewModelWithPositionAndSavedState() = runTest {
        val event1 = Event(comm = "Alpha", name = "First Event", time = LocalDateTime.of(2026, 8, 1, 12, 0))
        val event2 = Event(comm = "Beta", name = "Second Event", time = LocalDateTime.of(2026, 8, 2, 15, 0))
        events.add(event1, event2)

        val initialState = SavedStateHandle(mapOf(EventViewModel.EVENT_POSITION to 0))
        val viewModel = EventViewModel(initialState)
        assertEquals(event1, viewModel.event)

        viewModel.withPosition(1)
        assertEquals(event2, viewModel.event)
        assertEquals(1, initialState.get<Int>(EventViewModel.EVENT_POSITION))
    }
}
