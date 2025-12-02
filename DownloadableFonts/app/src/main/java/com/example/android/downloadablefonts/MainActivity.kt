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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArraySet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.core.graphics.Insets
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * This sample demonstrates how to use the Downloadable Fonts feature introduced in Android O.
 * Downloadable Fonts is a feature that allows apps to request a certain font from a provider
 * instead of bundling it or downloading it themselves. This means, there is no need to bundle the
 * font as an asset. See https://fonts.google.com/?sort=alpha for the fonts available, our choices
 * are in the [String] array with resource ID `R.array.family_names`.
 */
class MainActivity : AppCompatActivity() {

    /**
     * The [TextView] in our UI with resource ID `R.id.textview`. It contains text whose typeface is
     * changed to the one the user asks to be downloaded in the `onTypefaceRetrieved` override of the
     * [FontsContractCompat.FontRequestCallback] passed to [FontsContractCompat.requestFont] in our
     * [requestDownload] method.
     */
    private lateinit var mDownloadableFontTextView: TextView

    /**
     * The [SeekBar] in our UI with the ID `R.id.seek_bar_width` which is used to select the width
     * of the requested font. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml
     * which is included by our content view layout file layout/activity_main.xml Note: Persistent
     * bottom sheets are views that come up from the bottom of the screen, elevated over the main
     * content. They can be dragged vertically to expose more or less of their content.
     */
    private lateinit var mWidthSeekBar: SeekBar

    /**
     * The [SeekBar] in our UI with the ID `R.id.seek_bar_weight` which is used to select the weight
     * of the requested font. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml
     * which is included by our content view layout file layout/activity_main.xml Note: Persistent
     * bottom sheets are views that come up from the bottom of the screen, elevated over the main
     * content. They can be dragged vertically to expose more or less of their content.
     */
    private lateinit var mWeightSeekBar: SeekBar

    /**
     * The [SeekBar] in our UI with the ID `R.id.seek_bar_italic` which is used to select the italic
     * value (0f to 1f) of the requested font. It is in the bottomsheet layout file
     * layout/bottom_sheet_font_query.xml which is included by our content view layout file
     * layout/activity_main.xml Note: Persistent bottom sheets are views that come up from the
     * bottom of the screen, elevated over the main content. They can be dragged vertically to
     * expose more or less of their content.
     */
    private lateinit var mItalicSeekBar: SeekBar

    /**
     * The [CheckBox] in our UI with the ID `R.id.checkbox_best_effort` which is used to select the
     * value to use for the "&besteffort=" query parameter (`true` or `false`) of the requested font
     * URL. It is in the bottomsheet layout file layout/bottom_sheet_font_query.xml which is included
     * by our content view layout file layout/activity_main.xml Note: Persistent bottom sheets are
     * views that come up from the bottom of the screen, elevated over the main content. They can be
     * dragged vertically to expose more or less of their content.
     */
    private lateinit var mBestEffort: CheckBox

    /**
     * The [Button] in our UI with the ID `R.id.button_request` which when clicked will call our
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
     * array resource with ID `R.array.family_names` in our [onCreate] override. It is used only to
     * verify that the font name chosen in the [AutoCompleteTextView] used to select a font is a
     * valid font name by our [isValidFamilyName] method.
     */
    private lateinit var mFamilyNameSet: ArraySet<String>

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and set our content
     * view to our layout file `R.layout.activity_main`. It consists of a [CoordinatorLayout] root
     * view (a super-powered [FrameLayout]) which includes the layout file
     * `R.layout.bottom_sheet_font_query` (which is a A [FrameLayout] with a rounded corner
     * background and shadow) whose app:layout_behavior attribute is
     * [com.google.android.material.bottomsheet.BottomSheetBehavior] (ie. a bottom sheet with a peek
     * height of 120dp which can come up from the bottom of the screen, elevated over the main
     * content. It can be dragged vertically to expose more or less of its content) which holds a
     * [androidx.core.widget.NestedScrollView] holding all of the controls used to select and
     * configure the font to be requested. `R.layout.activity_main` also contains a [LinearLayout]
     * holding a [TextView] which displays sample text whose typeface is changed when a new font is
     * downloaded, and a [ProgressBar] used to display the progress of the font download.
     *
     * We initialize our [CoordinatorLayout] variable `rootView`
     * to the view with ID `R.id.container` then call
     * [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy
     * for applying window insets to `rootView`, with the `listener`
     * argument a lambda that accepts the [View] passed the lambda
     * in variable `v` and the [WindowInsetsCompat] passed the lambda
     * in variable `windowInsets`. It initializes its [Insets] variable
     * `insets` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument, then it updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `insets.left`, the right margin set to
     * `insets.right`, the top margin set to `insets.top`, and the bottom margin
     * set to `insets.bottom`. Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * Having set our content view we next call our [initializeSeekBars] method to have it locate
     * and configure all of the [SeekBar] controls used to change the characteristics of the font
     * we want to download.
     *
     * We initialize our [ArraySet] field [mFamilyNameSet] with a new instance then add all of the
     * strings in the `R.array.family_names` string array resource to it (our method
     * [isValidFamilyName] uses this [ArraySet] to verify that the font name the user chooses is
     * a valid font name).
     *
     * Next we initialize our [TextView] field [mDownloadableFontTextView] by finding the view with
     * ID `R.id.textview` (contains the sample text whose typeface will be changed to use the font
     * that is downloaded). We initialize our [ArrayAdapter] variable `val adapter` to an instance
     * which uses the layout file with ID [android.R.layout.simple_dropdown_item_1line] when
     * instantiating views, and the string array whose resource ID is `R.array.family_names` as the
     * objects to represent in the [ListView]. We initialize our [TextInputLayout] variable
     * `val familyNameInput` by finding the view with ID `R.id.auto_complete_family_name_input`
     * (it is a Layout which wraps a [AutoCompleteTextView] to show a floating label when the hint
     * is hidden while the user inputs text, and is used to display an error message if the user
     * tries to choose an invalid font). We initialize our [AutoCompleteTextView] variable
     * `val autoCompleteFamilyName` by finding the view with ID `R.id.auto_complete_family_name`
     * (it is the [AutoCompleteTextView] wrapped by `familyNameInput` which the user uses to choose
     * a font name). We then set hte adapter of `autoCompleteFamilyName` to `adapter` and add an
     * anonymous [TextWatcher] to it whose `onTextChanged` override uses our [isValidFamilyName]
     * method to determine if the text that the user typed in is valid and if it is valid disables
     * the error functionality of `familyNameInput` and clears the error message that will be
     * displayed below its [AutoCompleteTextView] `autoCompleteFamilyName`. If it is invalid the
     * override will enable the error functionality of `familyNameInput` and set the error message
     * that will be displayed below its [AutoCompleteTextView] `autoCompleteFamilyName` to "Not a
     * valid Family Name".
     *
     * Next we initialize our [Button] field [mRequestDownloadButton] by finding the view in our UI
     * with the ID `R.id.button_request` and set its [View.OnClickListener] to a lambda which
     * initializes its [String] variable `val familyName` to the `text` in `autoCompleteFamilyName`.
     * If our [isValidFamilyName] determines that it is not a valid family it enables the error
     * functionality of `familyNameInput` and sets the error message that will be displayed below its
     * [AutoCompleteTextView] `autoCompleteFamilyName` to "Not a valid Family Name" then toasts the
     * message "Invalid inputs exist". If [isValidFamilyName] determines that it is a valid family
     * name the lambda call our method [requestDownload] with `familyName` and disables the
     * [Button] field [mRequestDownloadButton].
     *
     * Finally our [onCreate] override initializes our [CheckBox] field [mBestEffort] by finding the
     * view with ID `R.id.checkbox_best_effort`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootView = findViewById<CoordinatorLayout>(R.id.container)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
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
            /**
             * This method is called to notify you that, within our [CharSequence] parameter
             * [charSequence] the [count] characters beginning at [start] are about to be replaced
             * by new text with length [after]. It is an error to attempt to make changes to
             * [charSequence] from this callback. We ignore.
             *
             * @param charSequence the [CharSequence] which is about to be modified.
             * @param start the starting index of the substring that will be replaced.
             * @param count the number of characters that will be replaced
             * @param after the length of the new text that is being added.
             */
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No op
            }

            /**
             * This method is called to notify you that, within [charSequence], the [before]
             * characters beginning at [start] have just replaced old text that had length
             * [count]. It is an error to attempt to make changes to [[charSequence]] from
             * this callback. If our [isValidFamilyName] determines that our [CharSequence]
             * parameter [charSequence] is a valid font family name we disable the error
             * functionality of our [TextInputLayout] variable `familyNameInput` and set the
             * error message that will be displayed below its [AutoCompleteTextView] to an
             * empty [String], and if [isValidFamilyName] determines that [charSequence] is not
             * a valid font family name enable the error functionality of our [TextInputLayout]
             * variable `familyNameInput` and set the error message that will be displayed below
             * its [AutoCompleteTextView] to the [String] "Not a valid Family Name".
             *
             * @param charSequence the [CharSequence] which has had a substring replaced.
             * @param start the starting index of the substring that was replaced.
             * @param before the number of characters that are new in [charSequence].
             * @param count the number of characters that got replaced.
             */
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (isValidFamilyName(charSequence.toString())) {
                    familyNameInput.isErrorEnabled = false
                    familyNameInput.error = ""
                } else {
                    familyNameInput.isErrorEnabled = true
                    familyNameInput.error = getString(R.string.invalid_family_name)
                }
            }

            /**
             * This method is called to notify you that, somewhere within our [Editable] parameter
             * [editable], the text has been changed. It is legitimate to make further changes to
             * [editable] from this callback, but be careful not to get yourself into an infinite
             * loop, because any changes you make will cause this method to be called again
             * recursively. We ignore.
             *
             * @param editable the [Editable] which has had text changed inside it.
             */
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
     * [FontsContractCompat.FontRequestCallback] passed to that method. We initialize our [QueryBuilder]
     * variable `val queryBuilder` to a new instance which is configured to use:
     *  - for its "&width=" query string the value that our [progressToWidth] calculates from the
     *  `progress` of our [SeekBar] field [mWidthSeekBar]
     *  - for its "&weight=" query string the value that our [progressToWeight] calculates from the
     *  `progress` of our [SeekBar] field [mWeightSeekBar]
     *  - for its "&italic=" query string the value that our [progressToItalic] calculates from the
     *  `progress` of our [SeekBar] field [mItalicSeekBar]
     *  - and for its "&besteffort=" query string the `true` or `false` value of the `isChecked`
     *  property of our [CheckBox] field [mBestEffort]
     *
     * We then initialize our [String] variable `val query` to the value returned by the
     * [QueryBuilder.build] method of `queryBuilder` and log the fact that we are about to
     * request that font. Next we initialize our [FontRequest] variable `val request` to a new
     * instance which uses the [String] "com.google.android.gms.fonts" as the authority of the Font
     * Provider to be used for the request, uses the [String] "com.google.android.gms" as the package
     * for the Font Provider to be used for the request (this is used to verify the identity of the
     * provider), uses `query` as the query to be sent over to the provider, and uses our resource
     * array with ID `R.array.com_google_android_gms_fonts_certs` as the resource array with the
     * list of sets of hashes for the certificates the provider should be signed with (this is used
     * to verify the identity of the provider). That last array is in our file values/font_certs.xml
     * and is an array of the string arrays `R.array.com_google_android_gms_fonts_certs_dev` and
     * `R.array.com_google_android_gms_fonts_certs_prod` which are defined in the same file.
     *
     * Next we intitialize our [ProgressBar] variable `val progressBar` by finding the view in our UI
     * with ID `R.id.progressBar` and set it to be visible. We initialize our variable `val callback`
     * to an anonymous [FontsContractCompat.FontRequestCallback] whose `onTypefaceRetrieved` override
     * sets the `typeface` property of our [TextView] field [mDownloadableFontTextView] to its [Typeface]
     * parameter `typeface`, sets the visibility of `progressBar` to [View.GONE] and enables our [Button]
     * field [mRequestDownloadButton], and whose `onTypefaceRequestFailed` override toasts the reason
     * number given for the font request failure, sets the visibility of `progressBar` to [View.GONE]
     * and enables our [Button] field [mRequestDownloadButton].
     *
     * Finally we call the [FontsContractCompat.requestFont] method with the `context` argument
     * this@MainActivity, the `request` argument our [FontRequest] variable `request`, the `style`
     * argument [Typeface.NORMAL], the `loadingExecutor` argument our [Executor] variable
     * `executorBackGround`, the `callbackExecutor` argument  an [Executor] that will run enqueued
     * tasks on the main thread associated with the MainActivity context, and the `callback` argument
     * our [FontsContractCompat.FontRequestCallback] variable `callback`.
     *
     * @param familyName the font family name that the user has requested.
     */
    private fun requestDownload(familyName: String) {
        val queryBuilder = QueryBuilder(familyName)
            .withWidth(progressToWidth(mWidthSeekBar.progress))
            .withWeight(progressToWeight(mWeightSeekBar.progress))
            .withItalic(progressToItalic(mItalicSeekBar.progress))
            .withBestEffort(mBestEffort.isChecked)
        val query: String = queryBuilder.build()
        Log.d(TAG, "Requesting a font. Query: $query")
        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            query,
            R.array.com_google_android_gms_fonts_certs
        )
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        val callback = object : FontsContractCompat.FontRequestCallback() {
            /**
             * Called then a [Typeface] request done via [FontsContractCompat.requestFont] is
             * complete. Note that this method will not be called if [onTypefaceRequestFailed]
             * is called instead. We set the `typeface` property of our [TextView] field
             * [mDownloadableFontTextView] to our [Typeface] parameter [typeface], set the
             * visibility of `progressBar` to [View.GONE] and enable our [Button] field
             * [mRequestDownloadButton],
             *
             * @param typeface  The [Typeface] object retrieved.
             */
            override fun onTypefaceRetrieved(typeface: Typeface) {
                mDownloadableFontTextView.typeface = typeface
                progressBar.visibility = View.GONE
                mRequestDownloadButton.isEnabled = true
            }

            /**
             * Called when a [Typeface] request done via [FontsContractCompat.requestFont] fails.
             * We toast the value of our [Int] parameter [reason], set the visibility of `progressBar`
             * to [View.GONE] and enable our [Button] field [mRequestDownloadButton].
             *
             * @param reason May be one of:
             *  - [FontsContractCompat.FontRequestCallback.FAIL_REASON_PROVIDER_NOT_FOUND] signals
             *  that given provider was not found on the device.
             *  - [FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_NOT_FOUND] signals that
             *  the font provider did not return any results for the given query.
             *  - [FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR] signals that
             *  the font returned by the provider was not loaded properly.
             *  - [FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_UNAVAILABLE] signals that
             *  the font provider found the queried font, but it is currently unavailable.
             *  - [FontsContractCompat.FontRequestCallback.FAIL_REASON_MALFORMED_QUERY] signals that
             *  the given query was not supported by the provider.
             *  - [FontsContractCompat.FontRequestCallback.FAIL_REASON_WRONG_CERTIFICATES] signals
             *  that the given provider must be authenticated and the given certificates do not
             *  match its signature.
             *  - or a provider defined positive code number.
             */
            override fun onTypefaceRequestFailed(reason: Int) {
                Toast.makeText(this@MainActivity,
                    getString(R.string.request_failed, reason), Toast.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                mRequestDownloadButton.isEnabled = true
            }
        }

        val executorBackGround: Executor = Executors.newSingleThreadExecutor()
        FontsContractCompat
            .requestFont(
                this@MainActivity,
                request,
                Typeface.NORMAL,
                executorBackGround,
                getMainExecutor(this@MainActivity),
                callback
            )
    }

    /**
     * Locates and configures all of the [SeekBar] widgets in our UI that are to be used to vary the
     * characteristics of the font that is requested. The [SeekBar] widgets are all in our bottom
     * sheet layout file layout/bottom_sheet_font_query.xml and are configured as follows:
     *  - "Width" is found at resource ID `R.id.seek_bar_width` to initialize our [SeekBar] field
     *  [mWidthSeekBar], its default `progress` property is calculated to initialize our variable
     *  `val widthValue` to be 100 times [Constants.WIDTH_DEFAULT] divided by [Constants.WIDTH_MAX],
     *  the [TextView] that displays its current value is found at resource ID `R.id.textview_width`
     *  to initialize our variable `val widthTextView` and the text of `widthTextView` is set to the
     *  [String] value of `widthValue`, and finally its [OnSeekBarChangeListener] is set to an anonymous
     *  instance whose `onProgressChanged` override sets the text of `widthTextView` to the width
     *  returned by our [progressToWidth] method for the current `progress` value passed the override,
     *  and whose `onStartTrackingTouch` and `onStopTrackingTouch` are no-ops.
     *  - "Weight" is found at resource ID `R.id.seek_bar_weight` to initialize our [SeekBar] field
     *  [mWeightSeekBar], its default `progress` property is calculated to initialize our variable
     *  `val weightValue` to be 100 times [Constants.WEIGHT_DEFAULT] divided by [Constants.WEIGHT_MAX],
     *  the [TextView] that displays its current value is found at resource ID `R.id.textview_weight`
     *  to initialize our variable `val weightTextView` and the text of `weightTextView` is set to the
     *  [String] value of `weightValue`, and finally its [OnSeekBarChangeListener] is set to an
     *  anonymous instance whose `onProgressChanged` override sets the text of `weightTextView` to
     *  the value returned by our [progressToWeight] method for the current `progress` value passed
     *  the override, and whose `onStartTrackingTouch` and `onStopTrackingTouch` are no-ops.
     *  - "Italic" is found at resource ID `R.id.seek_bar_italic` to initialize our [SeekBar] field
     *  [mItalicSeekBar], its default `progress` property is set to [Constants.ITALIC_DEFAULT], the
     *  [TextView] that displays its current value is found at resource ID `R.id.textview_italic`
     *  to initialize our variable `val italicTextView` and the text of `italicTextView` is set to
     *  [Constants.ITALIC_DEFAULT], and finally its [OnSeekBarChangeListener] is set to an anonymous
     *  instance whose `onProgressChanged` override sets the text of `progressToItalic` to the
     *  value returned by our [progressToItalic] method for the current `progress` value passed the
     *  override, and whose `onStartTrackingTouch` and `onStopTrackingTouch` are no-ops.
     */
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

    /**
     * Convenience function to check whether our [String] parameter [familyName] is among the font
     * family names in our [ArraySet] field [mFamilyNameSet] which is read from the string array
     * resource with ID `R.array.family_names` in our [onCreate] override. It is a subset of the
     * font family names actually avaiable from the provider but the only ones this demo allows the
     * user to chose from. We return `true` if [familyName] is not `null` and it exists in the set
     * [mFamilyNameSet], otherwise we return `false`.
     */
    private fun isValidFamilyName(familyName: String?): Boolean {
        return familyName != null && mFamilyNameSet.contains(familyName)
    }

    /**
     * Converts [progress] from a [SeekBar] to the value of width. If our parameter [progress] is 0 we
     * return 1f, otherwise we return [progress] times [Constants.WIDTH_MAX] divided by 100.
     *
     * @param progress is passed from 0 to 100 inclusive
     * @return the converted width
     */
    private fun progressToWidth(progress: Int): Float {
        return if (progress == 0) 1f else (progress * Constants.WIDTH_MAX / 100).toFloat()
    }

    /**
     * Converts [progress] from a [SeekBar] to the value of weight. For a [progress] of 0 we return
     * 1, and for a [progress] of 100 we return 1 less than [Constants.WEIGHT_MAX] (the range of the
     * weight is between (0, 1000) excluding the end points). For all other values we return
     * [Constants.WEIGHT_MAX] times [progress] divided by 100.
     *
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
     * Converts progress from a [SeekBar] to the value of italic. We return [progress] divided by
     * 100f.
     *
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
