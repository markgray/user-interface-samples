/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.samples.insetsanimation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.samples.insetsanimation.databinding.ActivityMainBinding

/**
 * The root activity for the sample. This Activity's layout contains a [ConversationFragment] which
 * is where the main entry point for this sample is.
 */
class MainActivity : AppCompatActivity() {
    /**
     * The [ActivityMainBinding] that is inflated from our layout file layout/activity_main.xml
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we initialize our [ActivityMainBinding] field [binding] to the value returned when the
     * [ActivityMainBinding.inflate] uses the [LayoutInflater] instance that this Window retrieved
     * from its [Context] to inflate its associated layout file layout/activity_main.xml, and we
     * set our content view to the outermost [View] in the layout file associated with [binding].
     * Finally we call the [WindowCompat.setDecorFitsSystemWindows] method with the current [Window]
     * of the activity and `false` for its `decorFitsSystemWindows` flag to tell the Window that our
     * app is going to be responsible for fitting any system windows.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
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

        // Tell the Window that our app is going to responsible for fitting for any system windows.
        // This is similar to the now deprecated:
        // view.setSystemUiVisibility(LAYOUT_STABLE | LAYOUT_FULLSCREEN | LAYOUT_FULLSCREEN)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
