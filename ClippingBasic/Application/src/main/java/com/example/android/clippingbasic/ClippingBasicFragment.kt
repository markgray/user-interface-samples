/*
 * Copyright 2014 The Android Open Source Project
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
package com.example.android.clippingbasic

import android.content.res.Resources
import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log

/**
 * This sample shows how to clip a [View] using an [Outline].
 */
class ClippingBasicFragment : Fragment() {
    /**
     * Store the click count so that we can show a different text on every click of our [TextView]
     * field [mTextView].
     */
    private var mClickCount = 0

    /**
     * The [Outline] used to clip the [FrameLayout] with ID [R.id.frame] which holds our [TextView]
     * field [mTextView].
     */
    private lateinit var mOutlineProvider: ViewOutlineProvider

    /**
     * An array of texts to display in our [TextView] field [mTextView]. They are cycled through
     * round robin when the [TextView] is clicked.
     */
    private lateinit var mSampleTexts: Array<String>

    /**
     * A reference to a [TextView] that shows different text strings when clicked.
     */
    private lateinit var mTextView: TextView

    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. Note that this can be called while the fragment's activity is still in the
     * process of being created. As such, you can not rely on things like the activity's content
     * view hierarchy being initialized at this point. If you want to do work once the activity
     * itself is created, see [onActivityCreated].
     *
     * First we call our super's implementation of `onCreate`, and call the [setHasOptionsMenu] with
     * `true` to report that this fragment would like to participate in populating the options menu
     * by receiving a call to [onCreateOptionsMenu] and related methods. We initialize our
     * [ViewOutlineProvider] field [mOutlineProvider] with a new instance of [ClipOutlineProvider],
     * and initialize our [Array] of [String] field [mSampleTexts] with the strings stored under the
     * resource ID [R.array.sample_texts] in our activity's [Resources].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mOutlineProvider = ClipOutlineProvider()
        mSampleTexts = resources.getStringArray(R.array.sample_texts)
    }

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and {@link [onActivityCreated]. It is recommended to *only* inflate the layout in
     * this method and move logic that operates on the returned View to [onViewCreated].
     *
     * We use our [LayoutInflater] parameter [inflater] to inflate the layout file whose resource ID
     * is [R.layout.clipping_basic_fragment] using our [ViewGroup] parameter [container] for its
     * `LayoutParams` without attaching to it and return the [View] that [inflater] returns to our
     * caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment,
     * @param container If non-`null`, this is the parent view that the fragment's UI will be
     * attached to. The fragment should not add the view itself, but this can be used to generate
     * the `LayoutParams` of the view.
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed from a
     * previous saved state as given here.
     * @return Return the [View] for the fragment's UI, or `null`.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.clipping_basic_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Set the initial text for the TextView. */
        mTextView = view.findViewById<View>(R.id.text_view) as TextView
        changeText()
        val clippedView = view.findViewById<View>(R.id.frame)

        /* Sets the OutlineProvider for the View. */
        clippedView.outlineProvider = mOutlineProvider

        /* When the button is clicked, the text is clipped or un-clipped. */
        view.findViewById<View>(R.id.button).setOnClickListener { bt ->
            // Toggle whether the View is clipped to the outline
            if (clippedView.clipToOutline) {
                /* The Outline is set for the View, but disable clipping. */
                clippedView.clipToOutline = false
                Log.d(TAG, String.format("Clipping to outline is disabled"))
                (bt as Button).setText(R.string.clip_button)
            } else {
                /* Enables clipping on the View. */
                clippedView.clipToOutline = true
                Log.d(TAG, String.format("Clipping to outline is enabled"))
                (bt as Button).setText(R.string.unclip_button)
            }
        }

        /* When the text is clicked, a new string is shown. */
        view.findViewById<View>(R.id.text_view).setOnClickListener {
            mClickCount++
            // Update the text in the TextView
            changeText()
            // Invalidate the outline just in case the TextView changed size
            clippedView.invalidateOutline()
        }
    }

    private fun changeText() {
        // Compute the position of the string in the array using the number of strings
        //  and the number of clicks.
        val newText = mSampleTexts[mClickCount % mSampleTexts.size]

        /* Once the text is selected, change the TextView */
        mTextView.text = newText
        Log.d(TAG, String.format("Text was changed."))
    }

    /**
     * A [ViewOutlineProvider] which clips the view with a rounded rectangle which is inset
     * by 10%
     */
    private inner class ClipOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val margin = view.width.coerceAtMost(view.height) / 10
            outline.setRoundRect(margin, margin, view.width - margin,
                view.height - margin, (margin / 2).toFloat())
        }
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "ClippingBasicFragment"
    }
}