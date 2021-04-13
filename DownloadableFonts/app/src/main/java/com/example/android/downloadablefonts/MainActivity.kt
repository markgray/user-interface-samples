/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.android.downloadablefonts

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArraySet
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import com.google.android.material.textfield.TextInputLayout

/**
 * This sample demonstrates how to use the Downloadable Fonts feature introduced in Android O.
 * Downloadable Fonts is a feature that allows apps to request a certain font from a provider
 * instead of bundling it or downloading it themselves. This means, there is no need to bundle the
 * font as an asset. See https://fonts.google.com/?sort=alpha for the fonts available, our choices
 * are in the [String] array with resource ID [R.array.family_names].
 */
class MainActivity : AppCompatActivity() {
    /**
     * The [Handler] we use to download a font. It is lazily initialized by our [handlerThreadHandler]
     * property to use the `looper` of a [HandlerThread] named "fonts" and then used in a call to
     * [FontsContractCompat.requestFont] by its [handlerThreadHandler] "alias" in our [requestDownload]
     * method so it is essentially the backing field for [handlerThreadHandler].
     */
    private var mHandler: Handler? = null

    /**
     * The [TextView] in our UI with resource ID [R.id.textview]. It contains text whose typeface is
     * changed to the one the user asks to be downloaded in the `onTypefaceRetrieved` override of the
     * [FontsContractCompat.FontRequestCallback] passed to [FontsContractCompat.requestFont] in our
     * [requestDownload] method.
     */
    private lateinit var mDownloadableFontTextView: TextView

    /**
     * The [SeekBar] in our UI with the ID [R.id.seek_bar_width] which is used to select the width
     * of the requested font. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml
     * which is included by our content view layout file layout/activity_main.xml Note: Persistent
     * bottom sheets are views that come up from the bottom of the screen, elevated over the main
     * content. They can be dragged vertically to expose more or less of their content.
     */
    private lateinit var mWidthSeekBar: SeekBar

    /**
     * The [SeekBar] in our UI with the ID [R.id.seek_bar_weight] which is used to select the weight
     * of the requested font. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml
     * which is included by our content view layout file layout/activity_main.xml Note: Persistent
     * bottom sheets are views that come up from the bottom of the screen, elevated over the main
     * content. They can be dragged vertically to expose more or less of their content.
     */
    private lateinit var mWeightSeekBar: SeekBar

    /**
     * The [SeekBar] in our UI with the ID [R.id.seek_bar_italic] which is used to select the italic
     * value (0f to 1f) of the requested font. It is in the bottomsheet layout file
     * layout/bottom_sheet_font_query.xml which is included by our content view layout file
     * layout/activity_main.xml Note: Persistent bottom sheets are views that come up from the
     * bottom of the screen, elevated over the main content. They can be dragged vertically to
     * expose more or less of their content.
     */
    private lateinit var mItalicSeekBar: SeekBar

    /**
     * The [CheckBox] in our UI with the ID [R.id.checkbox_best_effort] which is used to select the
     * value to use for the "&besteffort=" query parameter (`true` or `false`) of the requested font
     * URL. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml which is included
     * by our content view layout file layout/activity_main.xml Note: Persistent bottom sheets are
     * views that come up from the bottom of the screen, elevated over the main content. They can be
     * dragged vertically to expose more or less of their content.
     */
    private lateinit var mBestEffort: CheckBox

    /**
     * The [Button] in our UI with the ID [R.id.button_request] which when clicked will call our
     * [requestDownload] method to build a font request URL from the user's current choices and
     * call the [FontsContractCompat.requestFont] method to download that font. It is in the bottom
     * sheet layout file layout/bottom_sheet_font_query.xml which is included by our content view
     * layout file layout/activity_main.xml Note: Persistent bottom sheets are views that come up
     * from the bottom of the screen, elevated over the main content. They can be dragged vertically
     * to expose more or less of their content.
     */
    private lateinit var mRequestDownloadButton: Button

    /**
     * The [ArraySet] holding the names of the fonts we can download. It is read from the string
     * array resource with ID [R.array.family_names] in our [onCreate] override. It is used only to
     * verify that the font name chosen in the [AutoCompleteTextView] used to select a font is a
     * valid font name by our [isValidFamilyName] method.
     */
    private lateinit var mFamilyNameSet: ArraySet<String>

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_main]. It consists of a
     * [androidx.coordinatorlayout.widget.CoordinatorLayout] root view (a super-powered `FrameLayout`)
     * which includes the layout file [R.layout.bottom_sheet_font_query] (which is a A `FrameLayout`
     * with a rounded corner background and shadow) whose app:layout_behavior attribute is
     * [com.google.android.material.bottomsheet.BottomSheetBehavior] (ie. a bottom sheet with a peek
     * height of 120dp which can come up from the bottom of the screen, elevated over the main content.
     * It can be dragged vertically to expose more or less of its content) which holds a
     * [androidx.core.widget.NestedScrollView] holding all of the controls used to select and
     * configure the font to be requested. [R.layout.activity_main] also contains a `LinearLayout`
     * holding a [TextView] which displays sample text whose typeface is changed when a new font is
     * downloaded, and a [ProgressBar] used to display the progress of the font download.
     *
     * Having set our content view we next call our [initializeSeekBars] method to have it locate
     * and configure all of the [SeekBar] controls used to change the characteristics of the font
     * we want to download.
     *
     * We initialize our [ArraySet] field [mFamilyNameSet] with a new instance then add all of the
     * strings in the [R.array.family_names] string array resource to it (our method [isValidFamilyName]
     * uses this [ArraySet] to verify that the font name the user chooses is a valid font name).
     *
     * Next we initialize our [TextView] field [mDownloadableFontTextView] by finding the view with
     * ID [R.id.textview] (contains the sample text whose typeface will be changed to use the font
     * that is downloaded). We initialize our [ArrayAdapter] variable `val adapter` to an instance
     * which uses the layout file with ID [android.R.layout.simple_dropdown_item_1line] when
     * instantiating views, and the string array whose resource ID is [R.array.family_names] as the
     * objects to represent in the ListView. We initialize our [TextInputLayout] variable
     * `val familyNameInput` by finding the view with ID [R.id.auto_complete_family_name_input]
     * (it is a Layout which wraps a [AutoCompleteTextView] to show a floating label when the hint
     * is hidden while the user inputs text, and is used to display an error message if the user
     * tries to choose an invalid font). We initialize our [AutoCompleteTextView] variable
     * `val autoCompleteFamilyName` by finding the view with ID [R.id.auto_complete_family_name]
     * (it is the [AutoCompleteTextView] wrapped by `familyNameInput` which the user uses to choose
     * a font name). We then set hte adapter of `autoCompleteFamilyName` to `adapter` and add an
     * anonymous [TextWatcher] to it whose `onTextChanged` override uses our [isValidFamilyName]
     * method to determine if the text that the user typed in is valid and if it is valid disables
     * the error functionality of `familyNameInput` and clears the error message that will be displayed
     * below its [AutoCompleteTextView] `autoCompleteFamilyName`. If it is invalid the override will
     * enable the error functionality of `familyNameInput` and set the error message that will be
     * displayed below its [AutoCompleteTextView] `autoCompleteFamilyName` to "Not a valid Family
     * Name".
     *
     * Next we initialize our [Button] field [mRequestDownloadButton] by finding the view in our UI
     * with the ID [R.id.button_request] and set its [View.OnClickListener] to a lambda which
     * initializes its [String] variable `val familyName` to the `text` in `autoCompleteFamilyName`.
     * If our [isValidFamilyName] determines that it is not a valid family it enables the error
     * functionality of `familyNameInput` and sets the error message that will be displayed below its
     * [AutoCompleteTextView] `autoCompleteFamilyName` to "Not a valid Family Name" then toasts the
     * message "Invalid inputs exist". If [isValidFamilyName] determines that it is a valid family
     * name the lambda call our method [requestDownload] with `familyName` and disables the
     * [Button] field [mRequestDownloadButton].
     *
     * Finally our [onCreate] override initializes our [CheckBox] field [mBestEffort] by finding the
     * view with ID [R.id.checkbox_best_effort].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeSeekBars()
        mFamilyNameSet = ArraySet()
        mFamilyNameSet.addAll(listOf(*resources.getStringArray(R.array.family_names)))
        mDownloadableFontTextView = findViewById(R.id.textview)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.family_names)
        )
        val familyNameInput = findViewById<TextInputLayout>(R.id.auto_complete_family_name_input)
        val autoCompleteFamilyName = findViewById<AutoCompleteTextView>(
            R.id.auto_complete_family_name
        )
        autoCompleteFamilyName.setAdapter(adapter)
        autoCompleteFamilyName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No op
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                if (isValidFamilyName(charSequence.toString())) {
                    familyNameInput.isErrorEnabled = false
                    familyNameInput.error = ""
                } else {
                    familyNameInput.isErrorEnabled = true
                    familyNameInput.error = getString(R.string.invalid_family_name)
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // No op
            }
        })
        mRequestDownloadButton = findViewById(R.id.button_request)
        mRequestDownloadButton.setOnClickListener(View.OnClickListener {
            val familyName = autoCompleteFamilyName.text.toString()
            if (!isValidFamilyName(familyName)) {
                familyNameInput.isErrorEnabled = true
                familyNameInput.error = getString(R.string.invalid_family_name)
                Toast.makeText(
                    this@MainActivity,
                    R.string.invalid_input,
                    Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            requestDownload(familyName)
            mRequestDownloadButton.isEnabled = false
        })
        mBestEffort = findViewById(R.id.checkbox_best_effort)
    }

    /**
     * Constructs an URL for the font whose family name is our [String] parameter [familyName], with
     * query strings added to it for all of the options that the user has requested, then uses the
     * [FontsContractCompat.requestFont] method to download and then apply the new font using the
     * [FontsContractCompat.FontRequestCallback] passed to that method.
     *
     * @param familyName the font family name that the user has requested.
     */
    private fun requestDownload(familyName: String) {
        val queryBuilder = QueryBuilder(familyName)
            .withWidth(progressToWidth(mWidthSeekBar.progress))
            .withWeight(progressToWeight(mWeightSeekBar.progress))
            .withItalic(progressToItalic(mItalicSeekBar.progress))
            .withBestEffort(mBestEffort.isChecked)
        val query = queryBuilder.build()
        Log.d(TAG, "Requesting a font. Query: $query")
        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            query,
            R.array.com_google_android_gms_fonts_certs)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        val callback: FontsContractCompat.FontRequestCallback = object : FontsContractCompat.FontRequestCallback() {
            override fun onTypefaceRetrieved(typeface: Typeface) {
                mDownloadableFontTextView.typeface = typeface
                progressBar.visibility = View.GONE
                mRequestDownloadButton.isEnabled = true
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                Toast.makeText(this@MainActivity,
                    getString(R.string.request_failed, reason), Toast.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                mRequestDownloadButton.isEnabled = true
            }
        }
        FontsContractCompat
            .requestFont(this@MainActivity, request, callback, handlerThreadHandler)
    }

    private fun initializeSeekBars() {
        mWidthSeekBar = findViewById(R.id.seek_bar_width)
        val widthValue = (100 * Constants.WIDTH_DEFAULT.toFloat() / Constants.WIDTH_MAX.toFloat()).toInt()
        mWidthSeekBar.progress = widthValue
        val widthTextView = findViewById<TextView>(R.id.textview_width)
        widthTextView.text = widthValue.toString()
        mWidthSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                widthTextView.text = progressToWidth(progress).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mWeightSeekBar = findViewById(R.id.seek_bar_weight)
        val weightValue = Constants.WEIGHT_DEFAULT.toFloat() / Constants.WEIGHT_MAX.toFloat() * 100
        mWeightSeekBar.progress = weightValue.toInt()
        val weightTextView = findViewById<TextView>(R.id.textview_weight)
        weightTextView.text = Constants.WEIGHT_DEFAULT.toString()
        mWeightSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                weightTextView.text = progressToWeight(progress).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mItalicSeekBar = findViewById(R.id.seek_bar_italic)
        mItalicSeekBar.progress = Constants.ITALIC_DEFAULT.toInt()
        val italicTextView = findViewById<TextView>(R.id.textview_italic)
        italicTextView.text = Constants.ITALIC_DEFAULT.toString()
        mItalicSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {
                italicTextView.text = progressToItalic(progress).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun isValidFamilyName(familyName: String?): Boolean {
        return familyName != null && mFamilyNameSet.contains(familyName)
    }

    private val handlerThreadHandler: Handler
        get() {
            if (mHandler == null) {
                val handlerThread = HandlerThread("fonts")
                handlerThread.start()
                mHandler = Handler(handlerThread.looper)
            }
            return mHandler!!
        }

    /**
     * Converts progress from a SeekBar to the value of width.
     * @param progress is passed from 0 to 100 inclusive
     * @return the converted width
     */
    private fun progressToWidth(progress: Int): Float {
        return if (progress == 0) 1f else (progress * Constants.WIDTH_MAX / 100).toFloat()
    }

    /**
     * Converts progress from a SeekBar to the value of weight.
     * @param progress is passed from 0 to 100 inclusive
     * @return the converted weight
     */
    private fun progressToWeight(progress: Int): Int {
        return when (progress) {
            0 -> {
                1 // The range of the weight is between (0, 1000) (exclusive)
            }
            100 -> {
                Constants.WEIGHT_MAX - 1 // The range of the weight is between (0, 1000) (exclusive)
            }
            else -> {
                Constants.WEIGHT_MAX * progress / 100
            }
        }
    }

    /**
     * Converts progress from a SeekBar to the value of italic.
     * @param progress is passed from 0 to 100 inclusive.
     * @return the converted italic
     */
    private fun progressToItalic(progress: Int): Float {
        return progress.toFloat() / 100f
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "MainActivity"
    }
}