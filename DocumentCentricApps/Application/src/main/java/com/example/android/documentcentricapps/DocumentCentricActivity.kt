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
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

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
    private var mCheckbox: CheckBox? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_centric_main)
        mCheckbox = findViewById(R.id.multiple_task_checkbox)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        // Restore state from PersistableBundle
        if (persistentState != null) {
            mDocumentCounter = persistentState.getInt(KEY_EXTRA_NEW_DOCUMENT_COUNTER)
        }
    }

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

    @Suppress("UNUSED_PARAMETER")
    fun createNewDocument(view: View?) {
        val useMultipleTasks = mCheckbox!!.isChecked
        val newDocumentIntent = newDocumentIntent()
        if (useMultipleTasks) {
            /*
            When {@linkIntent#FLAG_ACTIVITY_NEW_DOCUMENT} is used with {@link Intent#FLAG_ACTIVITY_MULTIPLE_TASK}
            the system will always create a new task with the target activity as the root. This allows the same
            document to be opened in more than one task.
             */
            newDocumentIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
        startActivity(newDocumentIntent)
    }

    /**
     * Returns an new [Intent] to start [NewDocumentActivity] as a new document in
     * overview menu.
     *
     * To start a new document task [Intent.FLAG_ACTIVITY_NEW_DOCUMENT] must be used. The
     * system will search through existing tasks for one whose Intent matches the Intent component
     * name and the Intent data. If it finds one then that task will be brought to the front and the
     * new Intent will be passed to onNewIntent().
     *
     * Activities launched with the NEW_DOCUMENT flag must be created with launchMode="standard".
     */
    private fun newDocumentIntent(): Intent {
        val newDocumentIntent = Intent(this, NewDocumentActivity::class.java)
        newDocumentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        newDocumentIntent.putExtra(KEY_EXTRA_NEW_DOCUMENT_COUNTER, incrementAndGet())
        return newDocumentIntent
    }

    companion object {
        private const val TAG = "DocumentCentricActivity"
        const val KEY_EXTRA_NEW_DOCUMENT_COUNTER = "KEY_EXTRA_NEW_DOCUMENT_COUNTER"
        private var mDocumentCounter = 0
        private fun incrementAndGet(): Int {
            Log.d(TAG, "incrementAndGet(): $mDocumentCounter")
            return mDocumentCounter++
        }
    }
}