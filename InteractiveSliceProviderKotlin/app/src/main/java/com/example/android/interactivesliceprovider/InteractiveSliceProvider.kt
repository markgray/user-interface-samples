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
package com.example.android.interactivesliceprovider

import android.app.PendingIntent
import android.app.slice.Slice.EXTRA_RANGE_VALUE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.slice.Slice
import androidx.slice.SliceProvider
import com.example.android.interactivesliceprovider.data.DataRepository
import com.example.android.interactivesliceprovider.data.FakeDataSource
import com.example.android.interactivesliceprovider.slicebuilders.DefaultSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.GallerySliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.GridSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.InputRangeSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.ListSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.NoteSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.RangeSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.ReservationSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.RideSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.ToggleSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.WeatherSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.WifiSliceBuilder
import com.example.android.interactivesliceprovider.util.LazyFunctionMap
import com.example.android.interactivesliceprovider.util.buildUriWithAuthority

/**
 * Examples of using slice template builders.
 */
class InteractiveSliceProvider : SliceProvider() {

    private lateinit var repo: DataRepository
    private lateinit var contentNotifiers: LazyFunctionMap<Uri, Unit>

    private lateinit var hostNameUrl: String

    private lateinit var defaultPath: String
    private lateinit var wifiPath: String
    private lateinit var notePath: String
    private lateinit var ridePath: String
    private lateinit var togglePath: String
    private lateinit var galleryPath: String
    private lateinit var weatherPath: String
    private lateinit var reservationPath: String
    private lateinit var loadListPath: String
    private lateinit var loadGridPath: String
    private lateinit var inputRangePath: String
    private lateinit var rangePath: String

    /**
     * Implement this to initialize your slice provider on startup.
     *
     * @return `true` if the provider was successfully loaded, `false` otherwise
     */
    override fun onCreateSliceProvider(): Boolean {

        Log.d(TAG, "onCreateSliceProvider()")

        val contextNonNull = context ?: return false

        repo = DataRepository(FakeDataSource(Handler(Looper.myLooper()!!)))
        contentNotifiers = LazyFunctionMap {
            contextNonNull.contentResolver.notifyChange(it, null)
        }

        // Initialize Slice URL and all possible slice paths.
        hostNameUrl = contextNonNull.resources.getString(R.string.host_slice_url)

        defaultPath = contextNonNull.resources.getString(R.string.default_slice_path)

        wifiPath = contextNonNull.resources.getString(R.string.wifi_slice_path)
        notePath = contextNonNull.resources.getString(R.string.note_slice_path)

        ridePath = contextNonNull.resources.getString(R.string.ride_slice_path)
        togglePath = contextNonNull.resources.getString(R.string.toggle_slice_path)
        galleryPath = contextNonNull.resources.getString(R.string.gallery_slice_path)

        weatherPath = contextNonNull.resources.getString(R.string.weather_slice_path)
        reservationPath = contextNonNull.resources.getString(R.string.reservation_slice_path)
        loadListPath = contextNonNull.resources.getString(R.string.list_slice_path)

        loadGridPath = contextNonNull.resources.getString(R.string.grid_slice_path)
        inputRangePath = contextNonNull.resources.getString(R.string.input_slice_path)
        rangePath = contextNonNull.resources.getString(R.string.range_slice_path)

        return true
    }

    /**
     * Takes an Intent (as specified by the intent-filter in the manifest) with data
     * ("https://interactivesliceprovider.android.example.com/<your_path>") and returns a content
     * URI ("content://com.example.android.interactivesliceprovider/<your_path>").
     */
    override fun onMapIntentToUri(intent: Intent): Uri {

        @Suppress("UNNECESSARY_SAFE_CALL") // Better safe than sorry
        val intentPath = intent?.data?.path ?: "/"
        val uriWithoutPathSlash = intentPath.replace("/", "")

        val uri = context!!.buildUriWithAuthority(uriWithoutPathSlash)

        Log.d(TAG, "onMapIntentToUri(): \nintentPath: $intentPath \nuri:$uri")

        return uri
    }

    /**
     * Implemented to create a slice.
     *
     * @param sliceUri the [Uri] of the [Slice] we are to create
     * @return a [Slice] for our [Uri] parameter [sliceUri]
     */
    override fun onBindSlice(sliceUri: Uri?): Slice? {
        if (sliceUri == null || sliceUri.path == null) {
            return null
        }

        Log.d(TAG, "onBindSlice(): $sliceUri")

        return getSliceBuilder(sliceUri)?.buildSlice()
    }

    @Suppress("ReplaceNotNullAssertionWithElvisReturn") // When cannot have a return
    private fun getSliceBuilder(sliceUri: Uri): SliceBuilder? = when (sliceUri.path) {
        defaultPath -> DefaultSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        wifiPath -> WifiSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        notePath -> NoteSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        ridePath -> RideSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        togglePath -> ToggleSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        galleryPath -> GallerySliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        weatherPath -> WeatherSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        reservationPath -> ReservationSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        loadListPath -> ListSliceBuilder(
            context = context!!,
            sliceUri = sliceUri,
            repo = repo
        )

        loadGridPath -> GridSliceBuilder(
            context = context!!,
            sliceUri = sliceUri,
            repo = repo
        )

        inputRangePath -> InputRangeSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        rangePath -> RangeSliceBuilder(
            context = context!!,
            sliceUri = sliceUri
        )

        else -> {
            Log.e(TAG, "Unknown URI: $sliceUri")
            null
        }
    }

    /**
     * Called to inform an app that a slice has been pinned. Pinning is a way that slice hosts use
     * to notify apps of which slices they care about updates for. When a slice is pinned the
     * content is expected to be relatively fresh and kept up to date.
     *
     * Being pinned does not provide any escalated privileges for the slice provider. So apps should
     * do things such as turn on syncing or schedule a job in response to a onSlicePinned.
     *
     * Pinned state is not persisted through a reboot, and apps can expect a new call to
     * [onSlicePinned] for any slices that should remain pinned after a reboot occurs.
     *
     * @param sliceUri The uri of the slice being pinned.
     */
    override fun onSlicePinned(sliceUri: Uri?) {
        super.onSlicePinned(sliceUri)
        Log.d(TAG, "onSlicePinned - ${sliceUri?.path}")
        when (sliceUri?.path) {
            loadListPath -> repo.registerListSliceDataCallback(contentNotifiers[sliceUri])
            loadGridPath -> repo.registerGridSliceDataCallback(contentNotifiers[sliceUri])
            else -> Log.d(TAG, "No pinning actions for URI: $sliceUri")
        }
    }

    /**
     * Called to inform an app that a slices is no longer pinned. This means that no other apps on
     * the device care about updates to this slice anymore and therefore it is not important to be
     * updated. Any syncs or jobs related to this slice should be cancelled.
     *
     * @param sliceUri The uri of the slice being unpinned.
     */
    override fun onSliceUnpinned(sliceUri: Uri?) {
        super.onSliceUnpinned(sliceUri)
        Log.d(TAG, "onSliceUnpinned - ${sliceUri?.path}")
        when (sliceUri?.path) {
            loadListPath -> repo.unregisterListSliceDataCallbacks()
            loadGridPath -> repo.unregisterGridSliceDataCallbacks()
            else -> Log.d(TAG, "No unpinning actions for URI: $sliceUri")
        }
    }

    companion object {
        /**
         * TAG used for logging
         */
        const val TAG: String = "SliceProvider"

        /**
         * [Intent] action received by our [BroadcastReceiver] when the wifi status has changed
         */
        const val ACTION_WIFI_CHANGED: String = "com.example.androidx.slice.action.WIFI_CHANGED"

        /**
         * [Intent] action that causes a message stored under the key [EXTRA_TOAST_MESSAGE] to
         * be toasted.
         */
        const val ACTION_TOAST: String = "com.example.androidx.slice.action.TOAST"

        /**
         * Key under which a message [String] to be toasted is stored in an [Intent] with the action
         * [ACTION_TOAST].
         */
        const val EXTRA_TOAST_MESSAGE: String = "com.example.androidx.extra.TOAST_MESSAGE"

        /**
         * [Intent] action that causes a message to be toasted displaying the [Int] stored under
         * the key [EXTRA_RANGE_VALUE] to be displayed.
         */
        const val ACTION_TOAST_RANGE_VALUE: String = "com.example.androidx.slice.action.TOAST_RANGE_VALUE"

        /**
         * Creates a [PendingIntent] for the action given by its [String] parameter [action].
         *
         * @param context the [Context] we are running in.
         * @param action the action that we want the [PendingIntent] to request.
         * @return a [PendingIntent] for the [Intent] action [action].
         */
        fun getPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(action)
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }
}
