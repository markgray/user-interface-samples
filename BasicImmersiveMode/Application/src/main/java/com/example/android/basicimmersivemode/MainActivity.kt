/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.android.basicimmersivemode

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.android.common.activities.SampleActivityBase
import com.example.android.common.logger.Log
import com.example.android.common.logger.LogFragment
import com.example.android.common.logger.LogWrapper
import com.example.android.common.logger.MessageOnlyLogFilter

/**
 * Sample demonstrating the use of immersive mode to hide the system and navigation bars for
 * full screen applications.
 *
 * 'Immersive Mode' is a new UI mode which improves 'hide full screen' and 'hide nav bar'
 * modes, by letting users swipe the bars in and out.
 *
 * This sample demonstrates how to enable and disable immersive mode programmatically.
 *
 * Immersive mode was introduced in Android 4.4 (Api Level 19). It is toggled using the
 * SYSTEM_UI_FLAG_IMMERSIVE system ui flag. When combined with the SYSTEM_UI_FLAG_HIDE_NAVIGATION
 * and SYSTEM_UI_FLAG_FULLSCREEN  flags, hides the navigation and status bars and lets your app
 * capture all touch events on the screen.
 */
class MainActivity : SampleActivityBase() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * and then we set our content view to our layout file [R.layout.activity_main].
     *
     * If our [Bundle] parameter [savedInstanceState] is `null` we are being started for the first
     * time so we use the [FragmentManager] for interacting with fragments associated with this
     * activity to begin a [FragmentTransaction] which we save in our variable `val transaction`,
     * then we construct a new instance of [BasicImmersiveModeFragment] to initialize our variable
     * `val fragment`, use `transaction` to `replace` the contents of the container with ID
     * [FRAGTAG] with fragment and commit our [FragmentTransaction] variable `transaction`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this is not `null`, and any [Fragment] we added before being shut down will be
     * restored by the system. If it *is* `null` we are starting for the first time and need to
     * construct and add our [BasicImmersiveModeFragment] to our UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (supportFragmentManager.findFragmentByTag(FRAGTAG) == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = BasicImmersiveModeFragment()
            transaction.add(fragment, FRAGTAG)
            transaction.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /** Create a chain of targets that will receive log data  */
    override fun initializeLogging() {
        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper)

        // Filter strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter

        // On screen logging via a fragment with a TextView.
        val logFragment = supportFragmentManager
            .findFragmentById(R.id.log_fragment) as LogFragment?
        msgFilter.next = logFragment!!.logView
        logFragment.logView.setTextAppearance(R.style.Log)
        logFragment.logView.setBackgroundColor(Color.WHITE)
        Log.i(TAG, "Ready")
    }

    companion object {
        const val TAG = "MainActivity"
        const val FRAGTAG = "BasicImmersiveModeFragment"
    }
}