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
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

/**
 * Represents a "document" in the new overview notion. This is just a placeholder.
 * Real world examples of this could be:
 *
 *  * Document Editing
 *  * Browser tabs
 *  * Message composition
 *  * Sharing
 *  * Shopping item details
 *
 */
class NewDocumentActivity : AppCompatActivity() {
    /**
     * The [TextView] in our UI with ID `R.id.hello_new_document_text_view` which is used to display
     * our [Int] field [mDocumentCount] which is passed to use as an extra in the [Intent] which
     * [DocumentCentricActivity] uses to launch us.
     */
    private lateinit var mDocumentCounterTextView: TextView

    /**
     * The [Int] passed us as an extra in the [Intent] which [DocumentCentricActivity] uses to
     * launch us. We just display it in our [TextView] field [mDocumentCounterTextView].
     */
    private var mDocumentCount = 0

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`
     * then we set our content view to our layout file `R.layout.activity_new_document` (it consists
     * of a root `LinearLayout` holding our [TextView] field [mDocumentCounterTextView] and a
     * `Button` labeled "Remove from Overview" whose android:onClick attribute calls our method
     * [onRemoveFromOverview] to have it call [finishAndRemoveTask] to close our activity). We then
     * initialize our [Int] field [mDocumentCount] to the value stored as an extra in the [Intent]
     * that started this activity under the key [DocumentCentricActivity.KEY_EXTRA_NEW_DOCUMENT_COUNTER],
     * and initialize our [TextView] field [mDocumentCounterTextView] by finding the [View] in our
     * UI with the ID `R.id.hello_new_document_text_view`. Finally we call our [setDocumentCounterText]
     * method with the resource ID `R.string.hello_new_document_counter` (points to the [String]
     * "Hello Document %s!") to have it use that [String] to format [mDocumentCount] and display the
     * result as the text of [mDocumentCounterTextView].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_document)
        val rootView = findViewById<LinearLayout>(R.id.root_view_doc)
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
        mDocumentCount = intent
            .getIntExtra(DocumentCentricActivity.KEY_EXTRA_NEW_DOCUMENT_COUNTER, 0)
        mDocumentCounterTextView = findViewById(R.id.hello_new_document_text_view)
        setDocumentCounterText(R.string.hello_new_document_counter)
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the [Intent.FLAG_ACTIVITY_SINGLE_TOP] flag when calling [startActivity]. In
     * either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, [onNewIntent] will be called on the existing
     * instance with the [Intent] that was used to re-launch it.
     *
     * An activity can never receive a new intent in the resumed state. You can count on [onResume]
     * being called after this method, though not necessarily immediately after the completion of
     * this callback. If the activity was resumed, it will be paused and new intent will be
     * delivered, followed by [onResume]. If the activity wasn't in the resumed state, then new
     * intent can be delivered immediately, with [onResume] called sometime later when activity
     * becomes active again.
     *
     * Note that [getIntent] still returns the original [Intent]. You can use [setIntent] to update
     * it to this new [Intent].
     *
     * First we call our super's implementation of `onNewIntent`, then we call our [setDocumentCounterText]
     * method with the resource ID `R.string.reusing_document_counter` (points to the [String]
     * "Reusing Document %s!) to have it use that [String] to format [mDocumentCount] and display the
     * result as the text of [mDocumentCounterTextView].
     *
     * @param intent The new [Intent] that was started for the activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        /**
         * If `Intent.FLAG_ACTIVITY_MULTIPLE_TASK` has not been used this Activity will be reused.
         */
        setDocumentCounterText(R.string.reusing_document_counter)
    }

    /**
     * This is called by the android:onClick attribute of the `Button` in our UI labeled "Remove
     * from Overview", resource ID `R.id.remove_task_button` when the user clicks that `Button`.
     * We just call the [finishAndRemoveTask] method to have the system close our task and
     * completely remove it as a part of finishing the root activity of our task.
     *
     * @param view the [View] that was clicked.
     */
    @Suppress("UNUSED_PARAMETER") // Suggested change would make method less reusable
    fun onRemoveFromOverview(view: View?) {
        // It is good pratice to remove a document from the overview stack if not needed anymore.
        finishAndRemoveTask()
    }

    /**
     * Uses the [String] whose resource ID is our [Int] parameter [resId] as the format string to
     * format our [Int] field [mDocumentCount] and display the result as the text of our [TextView]
     * field [mDocumentCounterTextView].
     *
     * @param resId the resource ID of the format [String] we should use to format our [Int] field
     * [mDocumentCount] with.
     */
    private fun setDocumentCounterText(resId: Int) {
        mDocumentCounterTextView.text = String.format(getString(resId), mDocumentCount)
    }
}
