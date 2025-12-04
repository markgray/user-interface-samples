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
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.example.android.people.data.Contact
import com.example.android.people.databinding.MainActivityBinding
import com.example.android.people.ui.chat.ChatFragment
import com.example.android.people.ui.main.MainFragment
import com.example.android.people.ui.photo.PhotoFragment
import com.example.android.people.ui.viewBindings

/**
 * Entry point of the app when it is launched as a full app.
 */
class MainActivity : AppCompatActivity(R.layout.main_activity), NavigationController {

    companion object {
        /**
         * The name of the Fragment back stack for the chat screen.
         */
        private const val FRAGMENT_CHAT = "chat"
    }

    /**
     * The [MainActivityBinding] used to access the views in the layout.
     * Scoped to the lifecycle of the activity's view (which is instantiated in [onCreate]).
     */
    private val binding by viewBindings(MainActivityBinding::bind)

    /**
     * The transition to be used for the animation of the [androidx.appcompat.widget.Toolbar].
     */
    private lateinit var transition: Transition

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
     * `insets` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument, then it updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `insets.left`, the right margin set to
     * `insets.right`, the top margin set to `insets.top`, and the bottom margin
     * set to `insets.bottom`. Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * We configure the [androidx.appcompat.widget.Toolbar] as our app bar, inflate our `app_bar`
     * `Transition`, and if this is the first time we were created ([savedInstanceState] is `null`)
     * we add a [MainFragment] to our UI and handle the `Intent` that started us.
     *
     * Finally we call [requestNotificationPermission] to request permission from the user
     * to post notifications.
     *
     * @param savedInstanceState if this is `null` this is the first time we were called so we
     * use the `FragmentManager` to add a [MainFragment] to our UI, and if it is not `null` we
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
        setSupportActionBar(binding.toolbar)
        transition = TransitionInflater.from(this).inflateTransition(R.transition.app_bar)
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.container, MainFragment())
            }
            intent?.let(block = ::handleIntent)
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
     * This is called for activities that set launchMode to "singleTop" in their
     * manifest, or if a client used the [Intent.FLAG_ACTIVITY_SINGLE_TOP] flag when calling
     * [startActivity]. In either case, when the activity is re-launched while at the top of the
     * activity stack instead of a new instance of the activity being started, `onNewIntent()`
     * will be called on the existing instance with the Intent that was used to re-launch it.
     * We then handle this `intent` in our [handleIntent] method.
     *
     * @param intent The new intent that was started for the activity.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleIntent(intent = intent)
        }
    }

    /**
     * Handles the `Intent` that started the Activity.
     *
     * This function is called when the Activity is first created (in `onCreate`) or when a new
     * intent is delivered to an existing instance (in `onNewIntent`). It inspects the intent's
     * action to determine the desired behavior.
     *
     *  - [Intent.ACTION_VIEW]: This is typically triggered by clicking a dynamic or pinned shortcut.
     *  It extracts the contact ID from the intent's data URI and opens the corresponding chat screen.
     *  - [Intent.ACTION_SEND]: This is triggered by a "Direct Share" action from another app.
     *  It extracts the shortcut ID and the shared text, finds the corresponding contact, and opens
     *  the chat screen with the text pre-populated.
     *
     * @param intent The `Intent` to handle.
     */
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            // Invoked when a dynamic shortcut is clicked.
            Intent.ACTION_VIEW -> {
                val id = intent.data?.lastPathSegment?.toLongOrNull()
                if (id != null) {
                    openChat(id, null)
                }
            }
            // Invoked when a text is shared through Direct Share.
            Intent.ACTION_SEND -> {
                val shortcutId = intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID)
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                val contact = Contact.CONTACTS.find { it.shortcutId == shortcutId }
                if (contact != null) {
                    openChat(contact.id, text)
                }
            }
        }
    }

    /**
     * Updates the appearance of the app bar.
     *
     * This method can hide or show the app bar, and toggle between showing the generic app title
     * and the specific contact's name and icon. An optional lambda can be provided to further
     * customize the contact's name `TextView` and icon `ImageView`. A transition is used to animate
     * the changes.
     *
     * @param showContact `true` to show the contact's name and icon, `false` to show the default
     * app title. This has no effect if the app bar is hidden.
     * @param hidden `true` to hide the app bar entirely, `false` to show it.
     * @param body A lambda function that receives the `TextView` for the contact's name and the
     * `ImageView` for the contact's icon, allowing for further customization (e.g., setting the
     * text and image). This lambda is always executed, even if the views are not visible.
     */
    override fun updateAppBar(
        showContact: Boolean,
        hidden: Boolean,
        body: (name: TextView, icon: ImageView) -> Unit
    ) {
        if (hidden) {
            binding.appBar.visibility = View.GONE
        } else {
            binding.appBar.visibility = View.VISIBLE
            TransitionManager.beginDelayedTransition(binding.appBar, transition)
            if (showContact) {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                binding.name.visibility = View.VISIBLE
                binding.icon.visibility = View.VISIBLE
            } else {
                supportActionBar?.setDisplayShowTitleEnabled(true)
                binding.name.visibility = View.GONE
                binding.icon.visibility = View.GONE
            }
        }
        body(binding.name, binding.icon)
    }

    /**
     * Opens the chat screen for a specific contact.
     *
     * This function ensures that any existing chat screen is removed from the back stack before
     * adding a new one. This prevents multiple chat screens from piling up. It then creates a new
     * [ChatFragment] for the given contact `id` and replaces the current content in the `container`.
     *
     * @param id The ID of the contact to open the chat with.
     * @param prepopulateText An optional string to pre-fill in the chat's message input field.
     * This is typically used when sharing content to the chat from another app.
     */
    override fun openChat(id: Long, prepopulateText: String?) {
        supportFragmentManager.popBackStack(FRAGMENT_CHAT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
            addToBackStack(FRAGMENT_CHAT)
            replace(R.id.container, ChatFragment.newInstance(id, true, prepopulateText))
        }
    }

    /**
     * Opens a screen to display a full-sized photo.
     *
     * This function replaces the current content with a [PhotoFragment], which displays the
     * image identified by the provided [Uri]. The transaction is added to the back stack,
     * allowing the user to return to the previous screen.
     *
     * @param photo The [Uri] of the photo to be displayed.
     */
    override fun openPhoto(photo: Uri) {
        supportFragmentManager.commit {
            addToBackStack(null)
            replace(R.id.container, PhotoFragment.newInstance(photo))
        }
    }
}
