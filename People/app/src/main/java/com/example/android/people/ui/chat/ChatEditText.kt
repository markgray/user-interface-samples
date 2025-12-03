/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.people.ui.chat

// import androidx.core.widget.TextViewRichContentReceiverCompat
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * A listener for when an image is added to the chat.
 *
 * param contentUri The URI of the image.
 *
 * param mimeType The MIME type of the image.
 *
 * param label The label of the image.
 */
typealias OnImageAddedListener = (contentUri: Uri, mimeType: String, label: String) -> Unit

/**
 * A list of MIME types that are supported for insertion.
 */
@Suppress("unused") // Suggested change would make class less reusable
private val SUPPORTED_MIME_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/gif"
)

/**
 * A custom EditText with the ability to handle copy & paste of texts and images. This also works
 * with a software keyboard that can insert images.
 *
 * @param context The context of the view.
 * @param attrs The attributes of the view.
 * @param defStyleAttr The default style attribute of the view.
 */
class ChatEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    /**
     * A listener for receiving images inserted into the text box.
     */
    private var onImageAddedListener: OnImageAddedListener? = null

//  init {
//        richContentReceiverCompat = object : TextViewRichContentReceiverCompat() {
//            override fun onReceive(
//                textView: TextView,
//                clip: ClipData,
//                source: Int,
//                flags: Int
//            ): Boolean {
//                val mimeType = SUPPORTED_MIME_TYPES.find { clip.description.hasMimeType(it) }
//                return if (mimeType != null && clip.itemCount > 0) {
//                    onImageAddedListener?.invoke(
//                        clip.getItemAt(0).uri,
//                        mimeType,
//                        clip.description.label.toString()
//                    )
//                    true
//                } else {
//                    super.onReceive(textView, clip, source, flags)
//                }
//            }
//
//            override fun getSupportedMimeTypes(): Set<String> {
//                return SUPPORTED_MIME_TYPES + super.getSupportedMimeTypes()
//            }
//        }
//  }

    /**
     * Sets a listener to be called when a new image is added. This might be coming from copy &
     * paste or a software keyboard inserting an image.
     *
     * @param listener The listener to be called when a new image is added.
     */
    fun setOnImageAddedListener(listener: OnImageAddedListener?) {
        onImageAddedListener = listener
    }
}
