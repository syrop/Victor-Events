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

import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.comm.CommsDao
import pl.org.seva.events.event.Events
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.init.kodein
import pl.org.seva.events.main.ui.ColorFactory

@OptIn(ExperimentalCoroutinesApi::class)
class CommsTest {

    private lateinit var fsReader: FsReader
    private lateinit var fsWriter: FsWriter
    private lateinit var commsDao: CommsDao
    private lateinit var events: Events
    private lateinit var colorFactory: ColorFactory
    private lateinit var comms: Comms

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fsReader = mock(FsReader::class.java)
        fsWriter = mock(FsWriter::class.java)
        commsDao = mock(CommsDao::class.java)
        events = mock(Events::class.java)
        colorFactory = mock(ColorFactory::class.java)
        comms = Comms(fsReader, fsWriter, commsDao, events, colorFactory)
        kodein = DI {
            bindInstance { comms }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testJoin() = runTest {
        val comm = Comm("Alpha", "Alpha community", comms = comms)
        var updateCount = 0
        val job = launch { comms.updates.collect { updateCount++ } }

        assertTrue(comms.isEmpty)
        assertTrue(comms join comm)
        assertFalse(comms.isEmpty)
        assertEquals(1, comms.size)
        assertTrue(comm in comms)
        assertEquals(comm, comms[0])
        assertEquals(comm, comms["Alpha"])
        assertEquals(Comm.DUMMY, comms["NonExistent"])
        verify(events) addFrom comm
        verify(commsDao) add comm
        assertEquals(1, updateCount)

        assertFalse(comms join comm)
        assertEquals(1, comms.size)
        job.cancel()
    }

    @Test
    fun testLeave() = runTest {
        val comm = Comm("Alpha", "Alpha community", comms = comms)
        comms join comm
        var updateCount = 0
        val job = launch { comms.updates.collect { updateCount++ } }

        assertTrue(comms leave comm)
        assertTrue(comms.isEmpty)
        assertFalse(comm in comms)
        verify(commsDao) delete comm
        verify(events) deleteLocallyFrom comm
        assertEquals(1, updateCount)

        assertFalse(comms leave comm)
        job.cancel()
    }

    @Test
    fun testDelete() = runTest {
        val joined = Comm("Joined", comms = comms)
        val notJoined = Comm("NotJoined", comms = comms)
        comms join joined

        comms delete joined
        verify(fsWriter) delete joined
        assertFalse(joined in comms)

        comms delete notJoined
        verify(fsWriter, never()) delete notJoined
    }

    @Test
    fun testUpdate() = runTest {
        val initial = Comm("Community", desc = "Old description", comms = comms)
        val updated = Comm("Community", desc = "New description", comms = comms)
        comms join initial
        var updateCount = 0
        val job = launch { comms.updates.collect { updateCount++ } }

        comms update updated
        assertEquals("New description", comms["Community"].desc)
        verify(fsWriter) update updated
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testFromDbSortsByLowercaseName() = runTest {
        val commB = Comm("Beta", comms = comms)
        val commA = Comm("alpha", comms = comms)
        `when`(commsDao.getAllValues()).thenReturn(listOf(commB, commA))

        var updateCount = 0
        val job = launch { comms.updates.collect { updateCount++ } }

        comms.fromDb()
        assertEquals(2, comms.size)
        assertEquals(commA, comms[0])
        assertEquals(commB, comms[1])
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testAdminProperties() = runTest {
        val regularComm = Comm("Regular", isAdmin = false, comms = comms)
        val adminComm = Comm("AdminComm", isAdmin = true, comms = comms)

        assertFalse(comms.isAdminOfAny)
        comms join regularComm
        assertFalse(comms.isAdminOfAny)

        comms join adminComm
        assertTrue(comms.isAdminOfAny)
        assertArrayEquals(arrayOf("AdminComm"), comms.namesIsAdminOf)
    }

    @Test
    fun testJoinNewCommunity() = runTest {
        `when`(colorFactory.nextColor).thenReturn(Color.BLUE)

        val created = comms joinNewCommunity "NewComm"
        assertEquals("NewComm", created.name)
        assertEquals(Color.BLUE, created.color)
        assertTrue(created.isAdmin)
        assertTrue(created in comms)
        verify(fsWriter) create created
        verify(fsWriter) grantAdmin created
        verify(commsDao) add created
    }

    @Test
    fun testRefreshAndRefreshAdminStatuses() = runTest {
        val comm = Comm("MyComm", desc = "Old", color = Color.RED, isAdmin = false, comms = comms)
        comms join comm

        `when`(fsReader.isAdmin("mycomm")).thenReturn(true)
        comms.refreshAdminStatuses()
        testScheduler.advanceUntilIdle()
        assertTrue(comms["MyComm"].isAdmin)
        verify(commsDao).clear()

        val refreshedFromFs = Comm("MyComm", desc = "Updated", color = Color.GRAY, isAdmin = true, comms = comms)
        `when`(fsReader.findCommunity("MyComm")).thenReturn(refreshedFromFs)
        comms.refresh()
        testScheduler.advanceUntilIdle()
        assertEquals("Updated", comms["MyComm"].desc)
        assertEquals(Color.RED, comms["MyComm"].color)
    }

    @Test
    fun testCommEntityConversion() {
        val comm = Comm(name = "TestComm", desc = "Test Desc", color = Color.GREEN, isAdmin = true, comms = comms)
        val restored = Comm.Entity(comm).value()
        assertEquals(comm, restored)
    }
}
