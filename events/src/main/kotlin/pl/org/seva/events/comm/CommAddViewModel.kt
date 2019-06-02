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

package pl.org.seva.events.comm

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.data.firestore.fsReader
import pl.org.seva.events.main.extension.viewModel

val Fragment.commAddViewModel get() = viewModel<CommAddViewModel>()

class CommAddViewModel(val app: Application) : AndroidViewModel(app) {
    val queryState by lazy { MutableLiveData<QueryState>(QueryState.None) }
    val commToCreate by lazy { MutableLiveData<String?>() }
    private var queryJob: Job? = null

    fun query(name: String) {
        queryState.value = QueryState.InProgress
        queryJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                fsReader.findCommunity(name).let {
                    queryState.postValue(QueryState.Completed(it))
                }
            } catch (e: FirebaseFirestoreException) {
                queryState.postValue(QueryState.Error(app.getString(R.string.add_comm_connection_problem)))
            }
        }
    }

    fun resetQuery() {
        queryJob?.cancel()
        queryState.value = QueryState.None
    }

    sealed class QueryState {
        object None : QueryState()
        data class Completed(val comm: Comm) : QueryState()
        object InProgress : QueryState()
        data class Error(val errorMessage: String) : QueryState()
    }
}

