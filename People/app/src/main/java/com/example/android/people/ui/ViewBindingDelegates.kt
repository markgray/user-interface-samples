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

@file:Suppress("SameParameterValue")

package com.example.android.people.ui

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.viewbinding.ViewBinding

/*
 * Retrieves a view binding handle in an Activity.
 *
 * ```
 *     private val binding by viewBindings(MainActivityBinding::bind)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         binding.someView.someField = ...
 *     }
 * ```
 */


/**
 * A property delegate that provides a lazily-initialized [ViewBinding] for a [FragmentActivity].
 * The binding is created once and cached. The root view for the binding is obtained from the
 * activity's content view.
 *
 * This is useful for activities that use View Binding to avoid boilerplate code for inflating
 * and accessing views.
 *
 * Usage:
 * ```
 * class MyActivity : FragmentActivity() {
 *     private val binding by viewBindings(ActivityMyBinding::bind)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // The layout is inflated and set as the content view automatically.
 *         // You can now access views via the binding property.
 *         binding.textView.text = "Hello, View Binding!"
 *     }
 * }
 * ```
 *
 * @param BindingT The type of the generated binding class.
 * @param bind A lambda function that takes a [View] and returns an instance of the binding class.
 * This is typically a reference to the static `bind` method of the generated binding class
 * (e.g., `ActivityMyBinding::bind`).
 * @return A [Lazy] delegate that provides the [ViewBinding] instance.
 */
inline fun <reified BindingT : ViewBinding> FragmentActivity.viewBindings(
    crossinline bind: (View) -> BindingT
): Lazy<BindingT> = object : Lazy<BindingT> {

    /**
     * A backing field for the lazy-initialized view binding.
     * This property holds the cached [ViewBinding] instance. It is nullable and initialized to `null`.
     * When the `value` of the delegate is accessed for the first time, this field is populated with
     * the created binding instance. Subsequent accesses will return this cached instance directly.
     */
    private var cached: BindingT? = null

    /**
     * The lazily-initialized view binding instance.
     *
     * When accessed for the first time, this property will create the binding by invoking the
     * provided `bind` function. It uses the first child of the activity's content view
     * (the root view of the layout) for this purpose. The created binding instance is then
     * cached and returned on subsequent accesses.
     */
    override val value: BindingT
        get() = cached ?: bind(
            findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        ).also {
            cached = it
        }

    /**
     * Checks whether the view binding has been initialized.
     *
     * The binding is considered initialized if it has been created and cached at least once.
     * This method can be used to safely access the binding property only after it has been
     * initialized, for example, to avoid exceptions if accessed before `onCreate` completes.
     *
     * @return `true` if the [ViewBinding] instance has been created, `false` otherwise.
     */
    override fun isInitialized() = cached != null
}

/**
 * A property delegate that provides a lazily-initialized [ViewBinding] for a [Fragment].
 * The binding is tied to the fragment's view lifecycle. It is created when first accessed
 * after `onViewCreated` and is automatically cleared when the fragment's view is destroyed
 * (in `onDestroyView`).
 *
 * This helps to prevent memory leaks by ensuring that the binding does not hold a reference
 * to the view hierarchy when it's no longer valid.
 *
 * Usage:
 * ```
 * class MyFragment : Fragment(R.layout.my_fragment) {
 *     private val binding by viewBindings(MyFragmentBinding::bind)
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         // You can now safely access views via the binding property.
 *         binding.textView.text = "Hello from Fragment!"
 *     }
 * }
 * ```
 *
 * @param BindingT The type of the generated binding class.
 * @param bind A lambda function that takes a [View] and returns an instance of the binding class.
 * This is typically a reference to the static `bind` method of the generated binding class
 * (e.g., `MyFragmentBinding::bind`).
 * @return A [Lazy] delegate that provides the [ViewBinding] instance, scoped to the fragment's view lifecycle.
 * @throws IllegalStateException if the `value` is accessed when the fragment's view is not available
 * (e.g., before `onViewCreated` or after `onDestroyView`).
 */
inline fun <reified BindingT : ViewBinding> Fragment.viewBindings(
    crossinline bind: (View) -> BindingT
): Lazy<BindingT> = object : Lazy<BindingT> {

    /**
     * A backing field for the view binding.
     * This property is initialized when the view is created and cleared when the view is destroyed.
     */
    private var cached: BindingT? = null

    /**
     * An observer that listens to the fragment's view lifecycle events.
     * When the `ON_DESTROY` event is received (indicating that the fragment's view is
     * being destroyed), it clears the cached view binding instance to prevent memory leaks.
     */
    private val observer = LifecycleEventObserver { _, event: Lifecycle.Event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            cached = null
        }
    }

    /**
     * The lazily-initialized view binding instance.
     *
     * When accessed for the first time, this property creates the binding by invoking the
     * provided `bind` function with the fragment's `requireView()`. It then caches the
     * resulting binding instance.
     *
     * To prevent memory leaks, it also registers a `LifecycleEventObserver` that clears
     * the cached binding when the fragment's view lifecycle reaches the `ON_DESTROY` state.
     *
     * On subsequent accesses within the same view lifecycle, the cached instance is returned.
     *
     * @throws IllegalStateException if accessed when the fragment's view is not available
     * (e.g., before `onViewCreated` or after `onDestroyView`).
     */
    override val value: BindingT
        get() = cached ?: bind(requireView()).also {
            viewLifecycleOwner.lifecycle.addObserver(observer)
            cached = it
        }

    /**
     * Checks whether the view binding has been initialized.
     *
     * The binding is considered initialized if it has been created and cached. This method
     * can be used to safely access the binding property, especially in contexts where the
     * view might not be available, to avoid `IllegalStateException`.
     *
     * @return `true` if the [ViewBinding] instance has been created for the current view
     * lifecycle, `false` otherwise.
     */
    override fun isInitialized() = cached != null
}
