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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.comm.CommsDao
import pl.org.seva.events.event.Events
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.ui.ColorFactory

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class CommsTest {

    private lateinit var fsReader: FsReader
    private lateinit var fsWriter: FsWriter
    private lateinit var commsDao: CommsDao
    private lateinit var events: Events
    private lateinit var colorFactory: ColorFactory

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Before
    fun mockDependencies() {
        fsReader = mock(FsReader::class.java)
        fsWriter = mock(FsWriter::class.java)
        commsDao = mock(CommsDao::class.java)
        events = mock(Events::class.java)
        colorFactory = mock(ColorFactory::class.java)
    }

    @Test
    fun testJoin() = runBlockingTest {
        val comms = Comms(fsReader, fsWriter, commsDao, events, colorFactory)
        val comm = Comm("", "", comms = comms)
        val job = Job()
        @Suppress("UNCHECKED_CAST")
        val observer = mock(Observer::class.java) as Observer<Unit>
        comms.updatedLiveData(job).observeForever(observer)
        comms join comm
        Mockito.verify(events) addFrom comm
        Mockito.verify(commsDao) add comm
        Mockito.verify(observer).onChanged(Unit)
        job.cancel()
    }
}
