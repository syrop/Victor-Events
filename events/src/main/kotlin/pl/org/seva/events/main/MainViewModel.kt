/*
 * Copyright (C) 2018 Wiktor Nizio
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

package pl.org.seva.events.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.main.model.fs.fsReader

class MainViewModel : ViewModel() {
    val comm by lazy { MutableLiveData<Comm>() }
    val queryState by lazy { MutableLiveData<QueryState>().apply { value = QueryState.None }}
    val commToCreate by lazy { MutableLiveData<String?>() }
    private var queryJob: Job? = null

    fun query(name: String) {
        queryState.value = QueryState.WorkInProgress
        queryJob = viewModelScope.launch(Dispatchers.IO) {
            fsReader.findCommunity(name).let {
                if (isActive) {
                    queryState.postValue(QueryState.Comm(it))
                }
            }
        }
    }

    fun resetQuery() {
        queryJob?.cancel()
        queryState.value = QueryState.None
    }

    sealed class QueryState {
        object None : QueryState()
        data class Comm(val comm: pl.org.seva.events.comm.Comm) : QueryState()
        object WorkInProgress : QueryState()
    }
}
