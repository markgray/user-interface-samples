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

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.people.databinding.VoiceCallActivityBinding
import com.example.android.people.ui.viewBindings

/**
 * A dummy voice call screen. It only shows the icon and the name.
 */
class VoiceCallActivity : AppCompatActivity(R.layout.voice_call_activity) {

    companion object {
        /**
         *
         */
        const val EXTRA_NAME: String = "name"

        /**
         *
         */
        const val EXTRA_ICON_URI: String = "iconUri"
    }

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val rootView = findViewById<LinearLayout>(R.id.voice)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
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
        val name = intent.getStringExtra(EXTRA_NAME)
        val icon: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_ICON_URI, Uri::class.java)
        } else {
            @Suppress("DEPRECATION") // Needed for VERSION.SDK_INT < TIRAMISU
            intent.getParcelableExtra(EXTRA_ICON_URI)
        }
        if (name == null || icon == null) {
            finish()
            return
        }
        val binding: VoiceCallActivityBinding by viewBindings(VoiceCallActivityBinding::bind)
        binding.name.text = name
        Glide.with(binding.icon)
            .load(icon)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.icon)
    }
}
