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
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then initialize our [ActivityWindowDemosBinding] field [binding] by having the method
     * [ActivityWindowDemosBinding.inflate] use the [LayoutInflater] instance that this Window
     * retrieved from its [Context] to inflate the activity_window_demos.xml layout file
     * associated with it to produce an [ActivityWindowDemosBinding] instance, and we set our
     * content view to the outermost View in the layout file associated with [binding].
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
