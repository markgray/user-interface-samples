/*
 * Copyright 2020 The Android Open Source Project
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

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.example.windowmanagersample.databinding.ActivitySplitLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Demo of [SplitLayout]. */
class SplitLayoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplitLayoutBinding
    private lateinit var windowInfoRepo: WindowInfoTracker

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and initialize our
     * [Activity] variable `val activity` to `this`. We initialize our [ActivitySplitLayoutBinding]
     * field [binding] by having the method [ActivitySplitLayoutBinding.inflate] use the
     * [LayoutInflater] instance that this Window retrieved from its [Context] to inflate the
     * activity_split_layout.xml layout file associated with it to produce an
     * [ActivitySplitLayoutBinding] instance, and we set our content view to the outermost View in
     * the layout file associated with [binding].
     *
     * We call [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy
     * for applying window insets to the root [View] of [binding] `rootView`, with the
     * `listener` argument a lambda that accepts the [View] passed the lambda
     * in variable `v` and the [WindowInsetsCompat] passed the lambda
     * in variable `windowInsets`. It initializes its [Insets] variable
     * `systemBars` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument. It then gets the insets for the
     * IME (keyboard) using [WindowInsetsCompat.Type.ime]. It then updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `systemBars.left`, the right margin set to
     * `systemBars.right`, the top margin set to `systemBars.top`, and the bottom margin
     * set to the maximum of the system bars bottom inset and the IME bottom inset.
     * Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * We next initialize our [WindowInfoTracker] field [windowInfoRepo] to an instance of
     * [WindowInfoTracker] that is associated with our [Context]. Next we launch a coroutine on
     * the `lifecycleScope` [CoroutineScope] tied to this LifecycleOwner's Lifecycle. We call the
     * [Lifecycle.repeatOnLifecycle] method of our [Lifecycle] to have it execute its lambda block
     * when the lifecycle is at least STARTED (it is cancelled when the lifecycle is STOPPED, and
     * automatically restarted when the lifecycle is STARTED again). In that lambda block we call the
     * [WindowInfoTracker.windowLayoutInfo] method of our field [windowInfoRepo] to have it create
     * a [Flow] of [WindowLayoutInfo], on which we call our `throttleFirst` extension method to
     * have it delay the first event 10ms to allow the UI to pickup the posture, and then collect
     * the [WindowLayoutInfo] emitted by the [Flow] in order to pass it to the method
     * [SplitLayout.updateWindowLayout] of the [ActivitySplitLayoutBinding.splitLayout] view in
     * [binding].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val activity: Activity = this

        binding = ActivitySplitLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = systemBars.left
                rightMargin = systemBars.right
                topMargin = systemBars.top
                bottomMargin = systemBars.bottom.coerceAtLeast(ime.bottom)
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        windowInfoRepo = WindowInfoTracker.getOrCreate(this)
        // Create a new coroutine since repeatOnLifecycle is a suspend function
        lifecycleScope.launch {
            // The block passed to repeatOnLifecycle is executed when the lifecycle
            // is at least STARTED and is cancelled when the lifecycle is STOPPED.
            // It automatically restarts the block when the lifecycle is STARTED again.
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Safely collect from windowInfoRepo when the lifecycle is STARTED
                // and stops collection when the lifecycle is STOPPED
                windowInfoRepo.windowLayoutInfo(activity)
                    // Throttle first event 10ms to allow the UI to pickup the posture
                    .throttleFirst(10)
                    .collect { newLayoutInfo ->
                        // New posture information
                        val splitLayout = binding.splitLayout
                        splitLayout.updateWindowLayout(newLayoutInfo)
                    }
            }
        }
    }
}
