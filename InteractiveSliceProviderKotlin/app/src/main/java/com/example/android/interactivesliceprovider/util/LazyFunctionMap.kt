/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.interactivesliceprovider.util

/**
 * A map that lazily creates and caches [Runnable] objects.
 *
 * This class is useful for scenarios where you need to associate keys with actions (lambdas)
 * but want to avoid the overhead of creating a `Runnable` object for each action until it's
 * actually requested.
 *
 * The first time a `Runnable` is requested for a specific key via the `get` operator,
 * it is created by wrapping the provided [method] function and then cached. Subsequent requests
 * for the same key will return the cached `Runnable` instance.
 *
 * @param K The type of the keys in the map.
 * @param V The return type of the function to be executed.
 * @property method The function to be executed when the `Runnable` is run. It takes a key of
 * type [K] and returns a value of type [V].
 */
class LazyFunctionMap<K, V>(
    val method: (key: K) -> V) {

    /**
     * The backing map for this class.
     *
     * This map is used as a cache for the Runnables that are created. The first time a key
     * is accessed, a new Runnable is created, which wraps the [method] function, and is then
     * stored in this map. Subsequent accesses for the same key will return the cached Runnable.
     */
    private val map = hashMapOf<K, Runnable>()

    /**
     * Retrieves a [Runnable] for the given [key].
     *
     * If a [Runnable] for this [key] has not been created yet, this function will create a new
     * [Runnable] that executes the [method] with the provided [key], store it in the map for
     * future lookups, and then return it. If one already exists, the existing instance is
     * returned.
     *
     * @param key The key to retrieve the associated [Runnable] for.
     * @return The [Runnable] associated with the specified [key].
     */
    operator fun get(key: K): Runnable {
        var value = map[key]
        if (value == null) {
            value = Runnable {
                method(key)
            }
            map[key] = value
        }
        return value
    }
}
