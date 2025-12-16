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
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.example.android.people.ui.chat.ChatFragment
import com.example.android.people.ui.photo.PhotoFragment

/**
 * Entry point of the app when it is launched as an expanded Bubble.
 */
class BubbleActivity : AppCompatActivity(R.layout.bubble_activity), NavigationController {

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`.
     *
     * We initialize our [ContentFrameLayout] variable `rootView`
     * to the view with ID `android.R.id.content` then call
     * [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy
     * for applying window insets to `rootView`, with the `listener`
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
     * We retrieve the contact's ID from the intent's data URI and, if this is the first
     * creation (i.e., `savedInstanceState` is null), we add a [ChatFragment] to display
     * the conversation.
     *
     * @param savedInstanceState if this is `null` this is the first time we were called so we
     * use the `FragmentManager` to add a [ChatFragment] to our UI, and if it is not `null` we
     * are being recreated after a configuration change and the system will take care of restoring
     * our fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val rootView = window.decorView.findViewById<ContentFrameLayout>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
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

        val id = intent.data?.lastPathSegment?.toLongOrNull() ?: return
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.container, ChatFragment.newInstance(id, false))
            }
        }
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

    /**
     * This is not supported in this Activity because this Activity only shows a single chat thread.
     *
     * @param id The ID of the contact to open the chat with.
     * @param prepopulateText The text to prepopulate the message edit box with.
     * @throws UnsupportedOperationException
     */
    override fun openChat(id: Long, prepopulateText: String?) {
        throw UnsupportedOperationException("BubbleActivity always shows a single chat thread.")
    }

    /**
     * Opens the specified photo in a [PhotoFragment].
     *
     * @param photo The URI of the photo to open.
     */
    override fun openPhoto(photo: Uri) {
        // In an expanded Bubble, you can navigate between Fragments just like you would normally
        // do in a normal Activity. Just make sure you don't block onBackPressed().
        supportFragmentManager.commit {
            addToBackStack(null)
            replace(R.id.container, PhotoFragment.newInstance(photo))
        }
    }

    /**
     * The expanded bubble does not have an app bar, so we ignore calls to this method.
     *
     * @param showContact Whether to show the contact's name in the app bar.
     * @param hidden Whether the app bar should be hidden.
     * @param body A lambda that is called to customize the app bar.
     */
    override fun updateAppBar(
        showContact: Boolean,
        hidden: Boolean,
        body: (name: TextView, icon: ImageView) -> Unit
    ) {
        // The expanded bubble does not have an app bar. Ignore.
    }
}
