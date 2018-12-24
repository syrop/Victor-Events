package pl.org.seva.events.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

@Suppress("unused")
fun Disposable.neverDispose() = Unit

fun <T> Observable<T>.subscribe(lifecycle: Lifecycle, onNext: (T) -> Unit) =
        lifecycle.addObserver(RxLifecycleObserver { subscribe(onNext) })

private class RxLifecycleObserver(private val subscription: () -> Disposable) : LifecycleObserver {
    private lateinit var disposable: Disposable

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() { disposable = subscription() }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() = disposable.dispose()
}
