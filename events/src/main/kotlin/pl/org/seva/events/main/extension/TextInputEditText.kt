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

package pl.org.seva.events.main.extension

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import pl.org.seva.events.main.model.HotData

fun TextInputEditText.withLiveData(owner: LifecycleOwner, liveData: MutableLiveData<String>) {
    watch { liveData.value = text.toString() }
    liveData.observe(owner, observer)
}

operator fun TextInputEditText.plusAssign(hotData: HotData<String>) = withLiveData(hotData.owner, hotData.liveData)

private val TextInputEditText.observer get() = Observer { value: String? ->
    if (value == text.toString()) {
        return@Observer
    }
    setText(value)
}

private fun TextInputEditText.watch(afterTextChanged: Editable.() -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            s.afterTextChanged()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
    })
}
