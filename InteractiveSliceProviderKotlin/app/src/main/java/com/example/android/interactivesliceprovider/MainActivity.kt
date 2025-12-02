/*
 * Copyright 2018 The Android Open Source Project
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

@file:Suppress("DEPRECATION") // TODO: Replace PreferenceManager with the AndroidX Preference Library

package com.example.android.interactivesliceprovider

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.slice.SliceManager
import com.google.firebase.appindexing.FirebaseAppIndex

import java.net.URLDecoder

/**
 * Main activity for the Interactive Slices sample app.
 *
 * This activity handles the following:
 * 1. Granting slice permissions to other apps (like the Google Search App) so they can display
 *    the slices provided by this app's [SliceProvider].
 * 2. Providing a button to manually trigger Firebase App Indexing for the slices, making them
 *    discoverable.
 * 3. Handling window insets for edge-to-edge display.
 *
 * Note: The slice permission granting logic in this activity is for demonstration and development
 * purposes. In a production scenario, permissions are typically granted when a user interacts
 * with a slice.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created.
     *
     * This method initializes the main activity, including setting up the user interface,
     * granting permissions for the app's Slices, and handling window insets for edge-to-edge
     * display.
     *
     * The setup process includes:
     *  1.  `enableEdgeToEdge()`: Configures the activity to display its content underneath the
     *  system bars (status and navigation bars) for a more modern and immersive user
     *  experience.
     *  2.  `grantNonDefaultSlicePermission()`: Grants slice permissions for all non-default
     *  slice URIs defined in the app. This allows other apps, like the Google Search app,
     *  to display these slices. Note: This is a temporary requirement for the Early
     *  Access Program (EAP) and will not be needed in the public API.
     *  3.  Decodes the default slice URI from string resources to handle any special characters.
     *  4.  `grantSlicePermissions()`: Grants slice permissions for the decoded default slice URI.
     *  5.  `setContentView(R.layout.activity_main)`: Inflates the main layout for the activity.
     *  6.  `setOnApplyWindowInsetsListener`: Attaches a listener to the root view to handle
     *  system window insets. This ensures that UI elements are not obscured by system bars
     *  by applying the insets as margins to the root view.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down, this Bundle contains the data it most recently supplied in `onSaveInstanceState`.
     * Otherwise, it is null. This parameter is used by the superclass implementation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Grants permission to all non-default slices.
        // IMPORTANT NOTE: This will not be needed when the API is launched publicly. It is only
        // required at the current time for the EAP.
        grantNonDefaultSlicePermission()


        val defaultUriEncoded = resources.getString(R.string.default_slice_uri)

        // Decode for special characters that may appear in URI. Review Android documentation on
        // special characters for more information:
        // https://developer.android.com/guide/topics/resources/string-resource#FormattingAndStyling
        val defaultUriDecoded = URLDecoder.decode(defaultUriEncoded, "UTF-8")

        // Grants permission for default slice.
        grantSlicePermissions(defaultUriDecoded.toUri())

        setContentView(R.layout.activity_main)
        val rootView = findViewById<ConstraintLayout>(R.id.root_view)
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
    }

    /**
     * Grants permission to packages to access a slice URI.
     *
     * This method is a workaround for development and will NOT be needed for public launch.
     * It grants permission to the Google Search App (AGSA) and Google Mobile Services (GmsCore)
     * to display the slice referenced by the given URI.
     *
     * It also notifies a content change on the URI to trigger re-indexing, but only once
     * per app installation to avoid performance issues from frequent re-indexing. This is
     * tracked using SharedPreferences.
     *
     * @param uri The slice URI to grant permissions for.
     * @param notifyIndexOfChange If true, notifies a content change for the URI to prompt
     * re-indexing. Defaults to true.
     */
    private fun grantSlicePermissions(uri: Uri, notifyIndexOfChange: Boolean = true) {
        // Grant permissions to AGSA
        SliceManager.getInstance(this).grantSlicePermission(
            "com.google.android.googlequicksearchbox",
            uri
        )
        // grant permission to GmsCore
        SliceManager.getInstance(this).grantSlicePermission(
            "com.google.android.gms",
            uri
        )

        if (notifyIndexOfChange) {
            // Notify change. Ensure that it does not happen on every onCreate()
            // calls as notify change triggers reindexing which can clear usage
            // signals of your app and hence impact your app’s ranking. One way to
            // do this is to use shared preferences.
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext)

            if (!sharedPreferences.getBoolean(PREF_GRANT_SLICE_PERMISSION, false)) {
                contentResolver.notifyChange(uri, null /* content observer */)
                sharedPreferences.edit {
                    putBoolean(PREF_GRANT_SLICE_PERMISSION, true)
                }
            }
        }
    }

    /*
     * Grants permissions for non-default URLs, so they can be shown in google search on device.
     * IMPORTANT NOTE: As stated earlier, this will not be required soon (and not for launch), so
     * you can assume you won't need to loop through all your non-default slice URIs for the launch.
     */
    private fun grantNonDefaultSlicePermission() {

        /**
         * List of non-default slice URIs.
         */
        val nonDefaultUris = listOf(
            applicationContext.resources.getString(R.string.wifi_slice_uri),
            applicationContext.resources.getString(R.string.note_slice_uri),
            applicationContext.resources.getString(R.string.ride_slice_uri),
            applicationContext.resources.getString(R.string.toggle_slice_uri),
            applicationContext.resources.getString(R.string.gallery_slice_uri),
            applicationContext.resources.getString(R.string.weather_slice_uri),
            applicationContext.resources.getString(R.string.reservation_slice_uri),
            applicationContext.resources.getString(R.string.list_slice_uri),
            applicationContext.resources.getString(R.string.grid_slice_uri),
            applicationContext.resources.getString(R.string.input_slice_uri),
            applicationContext.resources.getString(R.string.range_slice_uri)
        )

        for (nonDefaultUri in nonDefaultUris) {
            grantSlicePermissions(
                nonDefaultUri.toUri(),
                false
            )
        }
    }

    /**
     * Handles the click event for the "Index Slices" button.
     *
     * This function initiates the Firebase App Indexing process for the app's slices.
     * It creates an intent with the action `FirebaseAppIndex.ACTION_UPDATE_INDEX` and
     * sends it as a broadcast to the `AppIndexingUpdateReceiver`. This receiver
     * then handles the task of updating the index, making the slices discoverable in
     * apps like the Google Search App.
     *
     * @param view The view that was clicked (the button). This parameter is required by
     * the `android:onClick` attribute in the layout XML but is not used within the function.
     */
    @Suppress("UNUSED_PARAMETER", "RedundantSuppression") // Suggested change would make class less reusable
    fun onClickIndexSlices(view: View) {
        val intent = Intent(this, AppIndexingUpdateReceiver::class.java)
        intent.action = FirebaseAppIndex.ACTION_UPDATE_INDEX
        sendBroadcast(intent)
    }

    companion object {
        /**
         * Key for a boolean shared preference that indicates whether slice permissions
         * have been granted and a content change has been notified. This is used to
         * ensure that the content change notification, which triggers re-indexing,
         * happens only once per app installation to avoid performance issues.
         */
        private const val PREF_GRANT_SLICE_PERMISSION = "permission_slice_status"

        /**
         * Creates a `PendingIntent` that launches the `MainActivity`.
         *
         * This is used as the primary action for some slices, allowing the user to
         * navigate from the slice to the main app activity. The `FLAG_IMMUTABLE`
         * flag is used to ensure the intent cannot be modified, which is a
         * security best practice.
         *
         * @param context The context to use for creating the `PendingIntent`.
         * @return A `PendingIntent` that will start `MainActivity`.
         */
        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }
}