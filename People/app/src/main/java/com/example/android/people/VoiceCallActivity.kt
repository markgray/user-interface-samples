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

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ContentFrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
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
         * Used as the key for the name of the contact in the intent extras.
         */
        const val EXTRA_NAME: String = "name"

        /**
         * Used as the key for the Uri of the contact in the intent extras.
         */
        const val EXTRA_ICON_URI: String = "iconUri"
    }

    /**
     * Called when the activity is first created. This function initializes the activity,
     * retrieves the contact's name and icon URI from the intent extras, and displays them
     * on the screen. If the required extras are not provided, the activity is finished.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * `onSaveInstanceState(Bundle)`. Otherwise it is `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
        val rootView = window.decorView.findViewById<ContentFrameLayout>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
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

        binding.name.text = name
        Glide.with(binding.icon)
            .load(icon)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.icon)
        requestNotificationPermission()
    }

    /**
     * Requests the [POST_NOTIFICATIONS] permission from the user.
     *
     * This function checks if the app has been granted the [POST_NOTIFICATIONS] permission.
     * If the permission has not been granted, it launches a system permission request dialog
     * using the [ActivityResultLauncher] property [actionRequestPermission] launcher. If the
     * permission is already granted, the function does nothing. This is necessary for apps targeting
     * Android 13 (API level 33) or higher to be able to post notifications.
     */
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            @SuppressLint("InlinedApi")
            actionRequestPermission.launch(arrayOf(POST_NOTIFICATIONS))
            return
        }
    }

    /**
     * An [ActivityResultLauncher] for requesting permissions.
     *
     * This launcher is initialized using [registerForActivityResult] with the
     * [ActivityResultContracts.RequestMultiplePermissions] contract. It is used to
     * launch the system's permission request dialog. The lambda provided is a callback
     * that will be executed when the user responds to the permission request, providing
     * a map of which permissions were granted. In this implementation, the callback is
     * empty as no specific action is needed immediately after the user's decision.
     *
     * @see requestNotificationPermission
     */
    private val actionRequestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            RequestMultiplePermissions()
        ) {}

}
