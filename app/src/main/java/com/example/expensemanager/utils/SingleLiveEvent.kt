package com.example.expensemanager.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
    // AtomicBoolean để đảm bảo thread-safe
    // true = có data chờ emit, false = đã emit rồi
    private val pending = AtomicBoolean(false)

    /**
     * Override observe để kiểm soát khi nào emit data
     */
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        // Log để debug (có thể bỏ trong production)
        if (hasActiveObservers()) {
            android.util.Log.w(
                "SingleLiveEvent",
                "Multiple observers registered but only one will be notified of changes."
            )
        }

        // Gọi super.observe với custom observer wrapper
        super.observe(owner) { t ->
            // Chỉ notify observer nếu pending = true
            // compareAndSet(expected, update):
            //   - Nếu pending = true (expected), set thành false và return true
            //   - Nếu pending = false, không làm gì và return false
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    /**
     * Override setValue để set pending flag
     */
    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)  // Đánh dấu có data mới
        super.setValue(t)   // Trigger observer
    }

    /**
     * Call này để trigger event mà không cần data
     * Useful cho events như "refresh", "close dialog"
     */
    @MainThread
    fun call() {
        value = null
    }
}