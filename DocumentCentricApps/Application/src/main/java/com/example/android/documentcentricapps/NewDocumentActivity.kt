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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
     * The [TextView] in our UI with ID [R.id.hello_new_document_text_view] which is used to display
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
     * then we set our content view to our layout file [R.layout.activity_new_document] (it consists
     * of a root `LinearLayout` holding our [TextView] field [mDocumentCounterTextView] and a
     * `Button` labeled "Remove from Overview" whose android:onClick attribute calls our method
     * [onRemoveFromOverview] to have it call [finishAndRemoveTask] to close our activity). We then
     * initialize our [Int] field [mDocumentCount] to the value stored as an extra in the [Intent]
     * that started this activity under the key [DocumentCentricActivity.KEY_EXTRA_NEW_DOCUMENT_COUNTER],
     * and initialize our [TextView] field [mDocumentCounterTextView] by finding the [View] in our
     * UI with the ID [R.id.hello_new_document_text_view]. Finally we call our [setDocumentCounterText]
     * with the resource ID [R.string.hello_new_document_counter] (points to the [String] "Hello
     * Document %s!") to have it use that [String] to format [mDocumentCount] and display the result
     * as the text of [mDocumentCounterTextView].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_document)
        mDocumentCount = intent
            .getIntExtra(DocumentCentricActivity.KEY_EXTRA_NEW_DOCUMENT_COUNTER, 0)
        mDocumentCounterTextView = findViewById(R.id.hello_new_document_text_view)
        setDocumentCounterText(R.string.hello_new_document_counter)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        /**
         * If `Intent.FLAG_ACTIVITY_MULTIPLE_TASK` has not been used this Activity will be reused.
         */
        setDocumentCounterText(R.string.reusing_document_counter)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRemoveFromOverview(view: View?) {
        // It is good pratice to remove a document from the overview stack if not needed anymore.
        finishAndRemoveTask()
    }

    private fun setDocumentCounterText(resId: Int) {
        mDocumentCounterTextView.text = String.format(getString(resId), mDocumentCount)
    }
}