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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.example.windowmanagersample.databinding.ActivityDisplayFeaturesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Demo activity that shows all display features and current device state on the screen. */
class DisplayFeaturesActivity : AppCompatActivity() {

    private val stateLog: StringBuilder = StringBuilder()

    private val displayFeatureViews = ArrayList<View>()

    private lateinit var binding: ActivityDisplayFeaturesBinding
    private lateinit var windowInfoRepo: WindowInfoTracker

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we initialize our [Activity] variable `val activity` to `this`. We initialize our
     * [ActivityDisplayFeaturesBinding] field [binding] by having the method
     * [ActivityDisplayFeaturesBinding.inflate] use the [LayoutInflater] instance that this Window
     * retrieved from its [Context] to inflate the activity_display_features.xml layout file
     * associated with it to produce an [ActivityDisplayFeaturesBinding] instance, and we set our
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
     * the [WindowLayoutInfo] emitted by the [Flow] in order to pass it to our [updateStateLog]
     * method and [updateCurrentState] method.
     *
     * Having launched our coroutine we proceed to call the `clear` extension function of our
     * [StringBuilder] field [stateLog] then append the string "State update log" and a newline
     * character to it.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: Activity = this

        binding = ActivityDisplayFeaturesBinding.inflate(layoutInflater)
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
                    .collect { newLayoutInfo: WindowLayoutInfo ->
                        // New posture information
                        updateStateLog(newLayoutInfo)
                        updateCurrentState(newLayoutInfo)
                    }
            }
        }

        stateLog.clear()
        stateLog.append(getString(R.string.state_update_log)).append("\n")
    }

    /** Updates the device state and display feature positions. */
    private fun updateCurrentState(layoutInfo: WindowLayoutInfo) {
        // Cleanup previously added feature views
        val rootLayout = binding.featureContainerLayout
        for (featureView in displayFeatureViews) {
            rootLayout.removeView(featureView)
        }
        displayFeatureViews.clear()

        // Update the UI with the current state
        val stateStringBuilder = StringBuilder()

        stateStringBuilder.append(getString(R.string.window_layout))
            .append(": ")

        // Add views that represent display features
        for (displayFeature in layoutInfo.displayFeatures) {
            val lp = getLayoutParamsForFeatureInFrameLayout(displayFeature, rootLayout)
                ?: continue

            // Make sure that zero-wide and zero-high features are still shown
            if (lp.width == 0) {
                lp.width = 1
            }
            if (lp.height == 0) {
                lp.height = 1
            }

            val featureView = View(this)
            val foldFeature = displayFeature as? FoldingFeature

            val color = if (foldFeature != null) {
                if (foldFeature.isSeparating) {
                    stateStringBuilder.append(getString(R.string.screens_are_separated))
                    getColor(R.color.color_feature_separating)
                } else {
                    stateStringBuilder.append(getString(R.string.screens_are_not_separated))
                    getColor(R.color.color_feature_not_separating)
                }
            } else {
                getColor(R.color.color_feature_unknown)
            }
            if (foldFeature != null) {
                stateStringBuilder
                    .append(" - ")
                    .append(
                        if (foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL) {
                            getString(R.string.screen_is_horizontal)
                        } else {
                            getString(R.string.screen_is_vertical)
                        }
                    )
            }
            featureView.foreground = color.toDrawable()

            rootLayout.addView(featureView, lp)
            featureView.id = View.generateViewId()

            displayFeatureViews.add(featureView)
        }

        binding.currentState.text = stateStringBuilder.toString()
    }

    /** Adds the current state to the text log of changes on screen. */
    private fun updateStateLog(layoutInfo: WindowLayoutInfo) {
        stateLog.append(getCurrentTimeString())
            .append(" ")
            .append(layoutInfo)
            .append("\n")
        binding.stateUpdateLog.text = stateLog
    }

    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        val currentDate = sdf.format(Date())
        return currentDate.toString()
    }
}
