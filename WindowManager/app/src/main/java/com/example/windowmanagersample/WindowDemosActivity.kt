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

import android.content.Context
import android.content.Intent
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
import com.example.windowmanagersample.databinding.ActivityWindowDemosBinding

/**
 * Main activity that launches WindowManager demos.
 */
class WindowDemosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWindowDemosBinding

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and initialize our
     * [ActivityWindowDemosBinding] field [binding] by having the method
     * [ActivityWindowDemosBinding.inflate] use the [LayoutInflater] instance that this Window
     * retrieved from its [Context] to inflate the activity_window_demos.xml layout file
     * associated with it to produce an [ActivityWindowDemosBinding] instance, and we set our
     * content view to the outermost View in the layout file associated with [binding].
     *
     * We call [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy
     * for applying window insets to the root [View] of [binding], with the `listener`
     * argument a lambda that accepts the [View] passed the lambda
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
     * We set the [View.OnClickListener] of the [ActivityWindowDemosBinding.featuresActivityButton]
     * button in [binding] to our [showDisplayFeatures] method and the [View.OnClickListener] of the
     * [ActivityWindowDemosBinding.splitLayoutActivityButton] button in [binding] to our
     * [showSplitLayout] method.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityWindowDemosBinding.inflate(layoutInflater)
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

        binding.featuresActivityButton.setOnClickListener { showDisplayFeatures() }
        binding.splitLayoutActivityButton.setOnClickListener { showSplitLayout() }
    }

    private fun showDisplayFeatures() {
        val intent = Intent(this, DisplayFeaturesActivity::class.java)
        startActivity(intent)
    }

    private fun showSplitLayout() {
        val intent = Intent(this, SplitLayoutActivity::class.java)
        startActivity(intent)
    }
}
