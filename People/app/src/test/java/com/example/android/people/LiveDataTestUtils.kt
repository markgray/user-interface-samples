/*
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.people

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Observes this [LiveData] for 3 seconds and returns the last emitted value.
 * This is a blocking method and should be used for testing only.
 *
 * @param T The type of the LiveData value.
 * @return The last value emitted by the [LiveData].
 * @throws NullPointerException if the [LiveData] value is null.
 * @throws InterruptedException if the 3-second waiting time is interrupted.
 */
fun <T> LiveData<T>.observedValue(): T {
    var result: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> {
        result = it
        latch.countDown()
    }
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        observeForever(observer)
    }
    latch.await(3000L, TimeUnit.MILLISECONDS)
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        removeObserver(observer)
    }
    return result!!
}
