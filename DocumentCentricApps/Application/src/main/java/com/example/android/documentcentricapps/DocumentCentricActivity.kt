/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.documentcentricapps

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

/**
 * DocumentCentricActivity shows the basic usage of the new Document-Centric Apps API. The new
 * API modifies the meaning of the [Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET] flag, which is
 * now deprecated. In versions before L it serves to define a boundary between the main task and a
 * subtask. The subtask holds a different thumbnail and all activities in it are finished when the
 * task is reset. In L this flag causes a full break with the task that launched it. As such it has
 * been renamed to [Intent.FLAG_ACTIVITY_NEW_DOCUMENT].
 *
 * This sample mainly uses Intent flags in code. But Activities can also specify in their manifests
 * that they shall always be launched into a new task in the above manner using the new activity
 * attribute documentLaunchMode which may take on one of three values, “intoExisting” equivalent to
 * NEW_DOCUMENT, “always” equivalent to NEW_DOCUMENT | MULTIPLE_TASK, “none” the default, and
 * “never” which will negate the effect of any attempt to launch the activity with NEW_DOCUMENT.
 */
class DocumentCentricActivity : AppCompatActivity() {
    /**
     * The [CheckBox] in our UI labeled "Task per document" with ID [R.id.multiple_task_checkbox].
     * When it is checked the flag [Intent.FLAG_ACTIVITY_MULTIPLE_TASK] is added to the [Intent]
     * used to launch [NewDocumentActivity]. This causes the system to create a new task and launch
     * [NewDocumentActivity] into it.
     */
    private lateinit var mCheckbox: CheckBox

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_document_centric_main],
     * and initialize our [CheckBox] field [mCheckbox] by finding the view in our UI with the ID
     * [R.id.multiple_task_checkbox]. Our UI consists of a root vertical `LinearLayout` holding two
     * vertical `LinearLayout`, the top one just holding a `TextView` displaying text describing the
     * demo, and the bottom one holding a `Button` labeled "Create new document" which launches a
     * new instance of [NewDocumentActivity], and a [CheckBox] labeled "Task per document" which the
     * user can use to add the flag [Intent.FLAG_ACTIVITY_MULTIPLE_TASK] the [Intent] used to launch
     * [NewDocumentActivity].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_centric_main)
        val rootView = findViewById<LinearLayout>(R.id.root_view)
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
        mCheckbox = findViewById(R.id.multiple_task_checkbox)
    }

    /**
     * This is the same as `onPostCreate(Bundle)` but is called for activities created with the
     * activity attribute android:persistableMode set to "persistAcrossReboots". It is called when
     * activity start-up is complete (ie. after [onStart] and [onRestoreInstanceState] have been
     * called).
     *
     * First we call our super's implementation of `onPostCreate`, then if our [PersistableBundle]
     * parameter [persistentState] is not `null` we set our static [Int] field [mDocumentCounter] to
     * the [Int] stored under the key [KEY_EXTRA_NEW_DOCUMENT_COUNTER] in [persistentState].
     *
     * @param savedInstanceState The data most recently supplied in [onSaveInstanceState]
     * @param persistentState The data coming from the [PersistableBundle] first saved in
     * [onSaveInstanceState]`(Bundle, PersistableBundle)`.
     */
    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        // Restore state from PersistableBundle
        if (persistentState != null) {
            mDocumentCounter = persistentState.getInt(KEY_EXTRA_NEW_DOCUMENT_COUNTER)
        }
    }

    /**
     * This is the same as [onSaveInstanceState]`(Bundle)` but is called for activities created with
     * the activity attribute android:persistableMode set to "persistAcrossReboots". The
     * [PersistableBundle] passed in will be saved and passed to the method
     * [onCreate]`(Bundle, PersistableBundle)` the first time that this activity is restarted
     * following the next device reboot. First we store our [Int] field [mDocumentCounter] in
     * our [PersistableBundle] parameter [outPersistentState] under the key
     * [KEY_EXTRA_NEW_DOCUMENT_COUNTER], then we call our super's implementation of
     * `onSaveInstanceState`
     *
     * @param outState [Bundle] in which to place your saved state.
     * @param outPersistentState State which will be saved across reboots.
     */
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        /*
        To maintain activity state across reboots the system saves and restore critical information for
        all tasks and their activities. Information known by the system includes the activity stack order,
        each task’s thumbnails and each activity’s and task's Intents. For Information that cannot be retained
        because they contain Bundles which can’t be persisted a new constrained version of Bundle,
        PersistableBundle is added. PersistableBundle can store only basic data types. To use it
        in your Activities you must declare the new activity:persistableMode attribute in the manifest.
         */
        outPersistentState.putInt(KEY_EXTRA_NEW_DOCUMENT_COUNTER, mDocumentCounter)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    /**
     * This is used as the value of the android:onClick attribute of the `Button` in our UI labeled
     * "Create new document" and will launch a new instance of [NewDocumentActivity]. First we
     * initialize our [Boolean] variable `val useMultipleTasks` to the `isChecked` property of our
     * [CheckBox] field [mCheckbox], and initialize our [Intent] variable `val newDocumentIntent` to
     * an [Intent] that will start [NewDocumentActivity] as a new document in the overview menu
     * that is returned by our [newDocumentIntent] method. If `useMultipleTasks` is `true` we add
     * the flag [Intent.FLAG_ACTIVITY_MULTIPLE_TASK] to `newDocumentIntent` (causes the system to
     * always create a new task with the target activity as the root).
     *
     * Finally we call the [startActivity] method to Launch the new [NewDocumentActivity] activity
     * as specified by our [Intent] variable `newDocumentIntent`.
     *
     * @param view the [View] that was clicked, we ignore.
     */
    @Suppress("UNUSED_PARAMETER") // Suggested change would make method less reusable
    fun createNewDocument(view: View?) {
        val useMultipleTasks: Boolean = mCheckbox.isChecked
        val newDocumentIntent: Intent = newDocumentIntent()
        if (useMultipleTasks) {
            /*
            When Intent.FLAG_ACTIVITY_NEW_DOCUMENT is used with Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            the system will always create a new task with the target activity as the root. This
             allows the same document to be opened in more than one task.
             */
            newDocumentIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
        startActivity(newDocumentIntent)
    }

    /**
     * Returns a new [Intent] to start [NewDocumentActivity] as a new document in the overview menu.
     * To start a new document task [Intent.FLAG_ACTIVITY_NEW_DOCUMENT] must be used. The system will
     * search through existing tasks for one whose [Intent] matches the [Intent] component name and
     * the [Intent] data. If it finds one then that task will be brought to the front and the new
     * [Intent] will be passed to `onNewIntent()`.
     *
     * Activities launched with the [Intent.FLAG_ACTIVITY_NEW_DOCUMENT] flag must be created with
     * launchMode="standard" (this is the default, so it is not specified in AndroidManifest.xml).
     *
     * First we initialize our [Intent] variable `val newDocumentIntent` to a new instance for the
     * specific component [NewDocumentActivity]. We then add to `newDocumentIntent` the flag
     * [Intent.FLAG_ACTIVITY_NEW_DOCUMENT] (opens a document into a new task rooted at the activity
     * launched by this [Intent]), and add as an extra the current value of [mDocumentCounter] that
     * is returned by our [incrementAndGet] method under the key [KEY_EXTRA_NEW_DOCUMENT_COUNTER]
     * ([incrementAndGet] post increments [mDocumentCounter]). Finally we return `newDocumentIntent`
     * to the caller.
     *
     * @return a new [Intent] to start [NewDocumentActivity] as a new document in the overview menu.
     */
    private fun newDocumentIntent(): Intent {
        val newDocumentIntent = Intent(this, NewDocumentActivity::class.java)
        newDocumentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        newDocumentIntent.putExtra(KEY_EXTRA_NEW_DOCUMENT_COUNTER, incrementAndGet())
        return newDocumentIntent
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "DocumentCentricActivity"

        /**
         * The [Intent] extra key which is used to pass the value of our static [Int] field
         * [mDocumentCounter] to [NewDocumentActivity] when it is launched, and which is also
         * used as the key which is used to store [mDocumentCounter] in the [PersistableBundle]
         * in our [onSaveInstanceState] method and to retrieve it in our [onPostCreate] method.
         */
        const val KEY_EXTRA_NEW_DOCUMENT_COUNTER: String = "KEY_EXTRA_NEW_DOCUMENT_COUNTER"

        /**
         * The number of the next [NewDocumentActivity] to be launched, it is passed under the key
         * [KEY_EXTRA_NEW_DOCUMENT_COUNTER] in the [Intent] used to launch a [NewDocumentActivity]
         */
        private var mDocumentCounter = 0

        /**
         * Post increments and returns our static [Int] field [mDocumentCounter], logging it as well.
         *
         * @return the current value of our our static [Int] field [mDocumentCounter]
         */
        private fun incrementAndGet(): Int {
            Log.d(TAG, "incrementAndGet(): $mDocumentCounter")
            return mDocumentCounter++
        }
    }
}