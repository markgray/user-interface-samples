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
     * value (0f to 1f) of the requested font. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml
     * which is included by our content view layout file layout/activity_main.xml Note: Persistent
     * bottom sheets are views that come up from the bottom of the screen, elevated over the main
     * content. They can be dragged vertically to expose more or less of their content.
     */
    private lateinit var mItalicSeekBar: SeekBar
    private lateinit var mBestEffort: CheckBox
    private lateinit var mRequestDownloadButton: Button
    private lateinit var mFamilyNameSet: ArraySet<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeSeekBars()
        mFamilyNameSet = ArraySet()
        mFamilyNameSet.addAll(listOf(*resources.getStringArray(R.array.family_names)))
        mDownloadableFontTextView = findViewById(R.id.textview)
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.family_names))
        val familyNameInput = findViewById<TextInputLayout>(R.id.auto_complete_family_name_input)
        val autoCompleteFamilyName = findViewById<AutoCompleteTextView>(
            R.id.auto_complete_family_name)
        autoCompleteFamilyName.setAdapter(adapter)
        autoCompleteFamilyName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int,
                                           after: Int) {
                // No op
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
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