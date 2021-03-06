/*
 * Copyright (C) 2017 Wiktor Nizio
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

package pl.org.seva.events.main.ui

import android.content.Context
import android.graphics.Color

open class ColorFactory(private val appContext: Context) {

    private val colors by lazy {
        with(appContext) {
            resources.getIdentifier(COLOR_ARRAY_NAME + COLOR_TYPE,"array", packageName).let {
                resources.obtainTypedArray(it)
            }
        }
    }

    val nextColor get() = with(colors) {
        val index = (Math.random() * length()).toInt()
        getColor(index, Color.GRAY)
    }

    companion object {
        const val COLOR_ARRAY_NAME = "mdcolor_"
        const val COLOR_TYPE = "400"
    }
}
