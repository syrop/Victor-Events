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
import org.kodein.di.DI
import org.kodein.di.bindInstance
import pl.org.seva.events.main.init.kodein
import pl.org.seva.events.message.Message
import pl.org.seva.events.message.Messages
import pl.org.seva.events.message.MessagesDao
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesTest {

    private lateinit var storedEntities: MutableList<Message.Entity>
    private lateinit var messagesDao: MessagesDao
    private lateinit var messages: Messages

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        storedEntities = mutableListOf()
        messagesDao = object : MessagesDao {
            override suspend fun getAll(): List<Message.Entity> = storedEntities.toList()

            override suspend fun insert(messages: Collection<Message.Entity>) {
                storedEntities.addAll(messages)
            }

            override suspend fun insert(message: Message.Entity) {
                storedEntities.add(message)
            }

            override suspend fun delete(message: Message.Entity) {
                storedEntities.removeAll { (it.time == message.time) && (it.content == message.content) }
            }
        }
        messages = Messages(messagesDao)
        kodein = DI {
            bindInstance { messages }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddAllAndGet() = runTest {
        val msg1 = Message(LocalDateTime.of(2026, 4, 1, 10, 0), "First message")
        val msg2 = Message(LocalDateTime.of(2026, 4, 2, 12, 0), "Second message")
        var updateCount = 0
        val job = launch { messages.updates.collect { updateCount++ } }

        assertTrue(messages.isEmpty())
        messages addAll listOf(msg1, msg2)
        assertFalse(messages.isEmpty())
        assertEquals(2, messages.size)
        assertEquals(msg1, messages[0])
        assertEquals(msg2, messages[1])
        assertEquals(listOf(msg1, msg2), storedEntities.map { it.value() })
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testReadFromDb() = runTest {
        val msg = Message(LocalDateTime.of(2026, 4, 3, 15, 30), "Persisted message")
        storedEntities.add(Message.Entity(msg))

        var updateCount = 0
        val job = launch { messages.updates.collect { updateCount++ } }

        messages.readFromDb()
        assertEquals(1, messages.size)
        assertEquals(msg, messages[0])
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testDelete() = runTest {
        val msg1 = Message(LocalDateTime.of(2026, 4, 1, 10, 0), "Keep me")
        val msg2 = Message(LocalDateTime.of(2026, 4, 2, 12, 0), "Delete me")
        messages addAll listOf(msg1, msg2)

        var updateCount = 0
        val job = launch { messages.updates.collect { updateCount++ } }

        msg2.delete()
        assertEquals(1, messages.size)
        assertEquals(msg1, messages[0])
        assertEquals(listOf(msg1), storedEntities.map { it.value() })
        assertEquals(1, updateCount)
        job.cancel()
    }

    @Test
    fun testMessageEntityConversion() {
        val msg = Message(LocalDateTime.of(2026, 7, 10, 8, 45), "Hello Events")
        val restored = Message.Entity(msg).value()
        assertEquals(msg, restored)
    }
}
