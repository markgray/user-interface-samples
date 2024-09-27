/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.textlinkify

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

/**
 * This sample demonstrates how clickable links can be added to a
 * [android.widget.TextView].
 *
 * This can be done in three ways:
 *
 *  * **Automatically:** Text added to a TextView can automatically be linkified by enabling
 * autoLinking. In XML, use the android:autoLink property, programatically call
 * [android.widget.TextView.setAutoLinkMask] using an option from
 * [android.text.util.Linkify]
 *
 *  * **Parsing a String as HTML:** See [android.text.Html.fromHtml])
 *
 *  * **Manually by constructing a [android.text.SpannableString]:** Consisting of
 * [android.text.style.StyleSpan] and [android.text.style.URLSpan] objects that
 * are contained within a [android.text.SpannableString]
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.sample_main]. We initialize our
     * [TextView] variable `val textViewResource` by finding the view with ID [R.id.text_html_resource],
     * set its text to stylized text created by [Html.fromHtml] from the string with resource ID
     * [R.string.link_text_manual], and set the [MovementMethod] of `textViewResource` to an instance
     * of [LinkMovementMethod] (A movement method that traverses links in the text buffer and scrolls
     * if necessary. Supports clicking on links with DPad Center or Enter). Next we initialize our
     * [TextView] variable `val textViewHtml` by finding the view with ID [R.id.text_html_program],
     * set its text to stylized text created by [Html.fromHtml] from a string that is generated by
     * the program, and also set the [MovementMethod] of `textViewHtml` to an instance of
     * [LinkMovementMethod]. We create a [SpannableString] to initialize our variable `val ss`
     * and then use the [SpannableString.setSpan] method to make the first 38 characters bold, then
     * we apply a [URLSpan] pointing to a telephone number to characters 45 to 49 making them
     * clickable. We initialize our [TextView] variable `val textViewSpan` by finding the view with
     * ID [R.id.text_spannable], set its text to `ss`, and set its [MovementMethod] to an instance
     * of [LinkMovementMethod].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_main)
        val rootView = findViewById<ScrollView>(R.id.root_view)
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

        // BEGIN_INCLUDE(text_auto_linkify)
        /*
         *  text_auto_linkify shows the android:autoLink property, which
         *  automatically linkifies things like URLs and phone numbers
         *  found in the text. No java code is needed to make this
         *  work.
         *  This can also be enabled programmatically by calling
         *  .setAutoLinkMask(Linkify.ALL) before the text is set on the TextView.
         *
         *  See android.text.util.Linkify for other options, for example only
         *  auto-linking email addresses or phone numbers
         */
        // END_INCLUDE(text_auto_linkify)

        // BEGIN_INCLUDE(text_html_resource)
        /*
         * text_html_resource has links specified by putting anchor tags (<a>) in the string
         * resource. By default these links will appear but not
         * respond to user input. To make them active, you need to
         * call setMovementMethod() on the TextView object.
         */
        val textViewResource = findViewById<TextView>(R.id.text_html_resource)
        textViewResource.text = Html.fromHtml(resources.getString(R.string.link_text_manual), 0)
        textViewResource.movementMethod = LinkMovementMethod.getInstance()
        // END_INCLUDE(text_html_resource)

        // BEGIN_INCLUDE(text_html_program)
        /*
         * text_html_program shows creating text with links from HTML in the Java
         * code, rather than from a string resource. Note that for a
         * fixed string, using a (localizable) resource as shown above
         * is usually a better way to go; this example is intended to
         * illustrate how you might display text that came from a
         * dynamic source (eg, the network).
         */
        val textViewHtml = findViewById<TextView>(R.id.text_html_program)
        textViewHtml.text = Html.fromHtml(
            "<b>text_html_program: Constructed from HTML programmatically.</b>"
                + "  Text with a <a href=\"http://www.google.com\">link</a> "
                + "created in the Java source code using HTML.",
            0
        )
        textViewHtml.movementMethod = LinkMovementMethod.getInstance()
        // END_INCLUDE(text_html_program)

        // BEGIN_INCLUDE(text_spannable)
        /*
         * text_spannable illustrates constructing a styled string containing a
         * link without using HTML at all. Again, for a fixed string
         * you should probably be using a string resource, not a
         * hardcoded value.
         */
        val ss = SpannableString(
            "text_spannable: Manually created spans. Click here to dial the phone.")

        /*
         * Make the first 38 characters bold by applying a StyleSpan with bold typeface.
         *
         * Characters 45 to 49 (the word "here") is made clickable by applying a URLSpan
         * pointing to a telephone number. Clicking it opens the "tel:" URL that starts the dialer.
         *
         * The SPAN_EXCLUSIVE_EXCLUSIVE flag defines this span as exclusive, which means
         * that it will not expand to include text inserted on either side of this span.
         */
        ss.setSpan(StyleSpan(Typeface.BOLD), 0, 39, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        ss.setSpan(
            URLSpan("tel:4155551212"),
            40 + 6, 40 + 10,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val textViewSpan = findViewById<TextView>(R.id.text_spannable)
        textViewSpan.text = ss

        /*
         * Set the movement method to move between links in this TextView.
         * This means that the user traverses through links in this TextView, automatically
         * handling appropriate scrolling and key commands.
         */
        textViewSpan.movementMethod = LinkMovementMethod.getInstance()
        // END_INCLUDE(text_spannable)
    }
}