/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.windowmanagersample

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Delays the emission of the first `T` of the [Flow] by [waitForMillis] milliseconds.
 *
 * @param waitForMillis the number of milliseconds to delay the first `T` by.
 */
fun <T> Flow<T>.throttleFirst(waitForMillis: Long): Flow<T> = flow {
    coroutineScope {
        val context = coroutineContext
        var delayPost: Deferred<Unit>? = null
        var throttleEvent = true
        collect {
            delayPost?.cancel()
            delayPost = async(Dispatchers.Default) {
                if (throttleEvent) {
                    delay(waitForMillis)
                }
                withContext(context) {
                    emit(it)
                    throttleEvent = false
                }
            }
        }
    }
}
