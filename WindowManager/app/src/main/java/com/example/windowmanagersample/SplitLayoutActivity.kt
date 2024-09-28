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
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we initialize our [Activity] variable `val activity` to `this`. We initialize our
     * [ActivitySplitLayoutBinding] field [binding] by having the method
     * [ActivitySplitLayoutBinding.inflate] use the [LayoutInflater] instance that this Window
     * retrieved from its [Context] to inflate the activity_split_layout.xml layout file
     * associated with it to produce an [ActivitySplitLayoutBinding] instance, and we set our
     * content view to the outermost View in the layout file associated with [binding]. We next
     * initialize our [WindowInfoTracker] field [windowInfoRepo] to an instance of [WindowInfoTracker]
     * that is associated with our [Context]. Next we launch a coroutine on the `lifecycleScope`
     * [CoroutineScope] tied to this LifecycleOwner's Lifecycle. We call the
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
                topMargin = insets.top
                bottomMargin = insets.bottom
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        setContentView(binding.root)

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
