package pl.org.seva.events.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

@Suppress("unused")
fun Disposable.neverDispose() = Unit

fun <T> Observable<T>.subscribe(lifecycle: Lifecycle, onNext: (T) -> Unit) =
        subscribe(onNext).observeLifecycle(lifecycle)

private fun Disposable.observeLifecycle(lifecycle: Lifecycle) =
        lifecycle.addObserver(RxLifecycleObserver(lifecycle, this))

private class RxLifecycleObserver(
        private val lifecycle: Lifecycle,
        private val subscription: Disposable) : LifecycleObserver {
    private val initialState = lifecycle.currentState

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private fun onEvent() {
        if (!lifecycle.currentState.isAtLeast(initialState)) {
            subscription.dispose()
        }
    }
}
