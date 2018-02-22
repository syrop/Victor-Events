package pl.org.seva.events.main

import android.content.Context
import android.widget.Toast

fun toaster() = instance<Toaster>()

class Toaster(private val ctx: Context) {

    fun toast(f: Context.() -> String) {
        Toast.makeText(ctx, ctx.f(), Toast.LENGTH_SHORT).show()
    }
}
