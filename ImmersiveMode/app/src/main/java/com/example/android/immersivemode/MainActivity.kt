/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.example.android.immersivemode

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams

/**
 * Behaviors of immersive mode. The [title] field is used as the items to be displayed by the
 * [ArrayAdapter] used as the `adapter` of the [Spinner] field `behaviorSpinner` (resource ID
 * `R.id.behavior` in our layout file), and the [Int] field [value] is the behavior constant that
 * the `systemBarsBehavior` is to be set to in the `controlWindowInsets` method.
 */
enum class BehaviorOption(
    /**
     * User friendly name of the behavior option
     */
    val title: String,
    /**
     * Behavior constant that the `systemBarsBehavior` is to be set to in the
     * `controlWindowInsets` method.
     */
    val value: Int
) {
    /**
     * Swipe from the edge to show a hidden bar.
     */
    ShowBarsBySwipe(
        "BEHAVIOR_DEFAULT",
        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    ),

    /**
     * Any interaction on the display to show the navigation bar.
     */
    ShowBarsByTouch(
        "BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE",
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    ),

    /**
     * "Sticky immersive mode". Swipe from the edge to temporarily reveal the hidden bar.
     */
    ShowTransientBarsBySwipe(
        "BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE",
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    )
}

/**
 * Type of system bars to hide or show. The [title] field is used as the items to be displayed by the
 * [ArrayAdapter] used as the `adapter` of the [Spinner] field `TypeOption` (resource ID `R.id.type`
 * in our layout file), and the [Int] field [value] is the type constant that is passed to the
 * [WindowInsetsControllerCompat] `hide` or `show` method in the `controlWindowInsets` method.
 */
enum class TypeOption(
    /**
     * User friendly string describing the system bars to hide or show.
     */
    val title: String,
    /**
     * Type constant that is passed to the[WindowInsetsControllerCompat] `hide` or `show` method in
     * the `controlWindowInsets` method.
     */
    val value: Int
) {
    /**
     * Both the status bar and the navigation bar
     */
    SystemBars(
        "systemBars()",
        WindowInsetsCompat.Type.systemBars()
    ),

    /**
     * The status bar only.
     */
    StatusBar(
        "statusBars()",
        WindowInsetsCompat.Type.statusBars()
    ),

    /**
     * The navigation bar only.
     */
    NavigationBar(
        "navigationBars()",
        WindowInsetsCompat.Type.navigationBars()
    )
}

/**
 * This demo demonstrates "Immersive mode", which is intended for apps in which users will be heavily
 * interacting with the screen. It uses [WindowInsetsControllerCompat] and has [Spinner]s which allow
 * the user to select the behavior of the system bars when they are hidden (labeled "Behavior" and
 * which of the bars are hidden (labeled "Type").
 */
class MainActivity : AppCompatActivity() {
    /**
     * The [Spinner] in our layout file with ID `R.id.behavior` (labeled "Behavior") used to select
     * the behavior of the system bars when they are hidden:
     *  - [WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE] When system bars are hidden in
     *  this mode, they can be revealed with system gestures, such as swiping from the edge of the
     *  screen where the bar is hidden from.
     *  - [WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH] System bars will be forcibly
     *  shown on any user interaction on the corresponding display.
     *  - [WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE] When system bars are
     *  hidden in this mode, they can be revealed temporarily with system gestures, such as swiping
     *  from the edge of the screen where the bar is hidden from. These transient system bars will
     *  overlay app’s content, may have some degree of transparency, and will automatically hide
     *  after a short timeout.
     */
    private lateinit var behaviorSpinner: Spinner

    /**
     * The [Spinner] in our layout file with ID `R.id.type` (labeled "Type") used to select which of
     * the system bars are hidden:
     *  - [WindowInsetsCompat.Type.systemBars] All system bars. Includes statusBars(), captionBar()
     *  as well as navigationBars(), but not ime().
     *  - [WindowInsetsCompat.Type.statusBars] An insets type representing any system bars for
     *  displaying status.
     *  - [WindowInsetsCompat.Type.navigationBars] An insets type representing any system bars for
     *  navigation.
     */
    private lateinit var typeSpinner: Spinner

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file `R.layout.main_activity` whose root view is
     * a vertical [LinearLayout] holding a:
     *  - Horizontal [LinearLayout] holding a "HIDE" and a "SHOW" [Button] (ID `R.id.hide` and
     *  `R.id.show`)
     *  - A [TextView] displaying the text "Behavior", above a [Spinner] with ID `R.id.behavior`
     *  - A [TextView] displaying the text "Type", above a [Spinner] with ID `R.id.type`
     *
     * Next we initialize our [Spinner] field [behaviorSpinner] by finding the view in our UI with
     * ID `R.id.behavior` and set its `adapter` to a new instance of [ArrayAdapter] which displays
     * the `title` property of the constants of the [BehaviorOption] `enum` class in the system layout
     * file [android.R.layout.simple_list_item_1], and we initialize our [Spinner] field [typeSpinner]
     * by finding the view in our UI with ID `R.id.type` and set its `adapter` to a new instance of
     * [ArrayAdapter] which displays the `title` property of the constants of the [TypeOption] `enum`
     * class in the system layout file [android.R.layout.simple_list_item_1].
     *
     * We initialize our [Button] variable `val hideButton` by finding the view in our UI with ID
     * `R.id.hide` and set its [View.OnClickListener] to a lambda which calls our [controlWindowInsets]
     * method with `true` to have it hide the system bars the user selected using [typeSpinner] with
     * the behavior he selected using [behaviorSpinner], and we initialize our [Button] variable
     * `val showButton` by finding the view in our UI with ID `R.id.show` and set its [View.OnClickListener]
     * to a lambda which calls our [controlWindowInsets] method with `false` to have it show the system
     * bars the user selected using [typeSpinner] with the behavior he selected using [behaviorSpinner].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val rootView = findViewById<LinearLayout>(R.id.main)
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

        behaviorSpinner = findViewById(R.id.behavior)
        behaviorSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            BehaviorOption.entries.map { it.title }
        )

        typeSpinner = findViewById(R.id.type)
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            TypeOption.entries.map { it.title }
        )

        val hideButton: Button = findViewById(R.id.hide)
        hideButton.setOnClickListener { controlWindowInsets(true) }
        val showButton: Button = findViewById(R.id.show)
        showButton.setOnClickListener { controlWindowInsets(false) }
    }

    /**
     * Hides (if our [Boolean] parameter [hide] is `true`) or shows (if it is `false`) the system
     * bars the user selected using [typeSpinner] with the behavior he selected using [behaviorSpinner].
     * First we initialize our [WindowInsetsControllerCompat] variable `val insetsController` with a
     * new instance constructed for the current [Window] of the activity and its top-level window
     * decor view (containing the standard window frame/decorations and the client's content inside
     * of that).
     *
     * We initialize our [Int] variable `val behavior` to the `value` property of the selected
     * [BehaviorOption] in the [Spinner] field [behaviorSpinner], and initialize our [Int] variable
     * `val type` to the `value` property of the selected [TypeOption] in the [Spinner] field
     * [typeSpinner]. We set the `systemBarsBehavior` property of `insetsController` to `behavior`
     * (determines how the bars behave when being hidden by the application), then we branch on the
     * value of our [Boolean] parameter [hide]:
     *  - `true` we call the [WindowInsetsControllerCompat.hide] method of `insetsController` to have
     *  it hide the system bars specified by `type` (a bitmask of [WindowInsetsCompat.Type])
     *  - `false` we call the [WindowInsetsControllerCompat.show] method of `insetsController` to
     *  have it show the system bars specified by `type` (a bitmask of [WindowInsetsCompat.Type])
     *
     * @param hide if `true` hide the system bars, if `false` show the system bars.
     */
    private fun controlWindowInsets(hide: Boolean) {
        // WindowInsetsController can hide or show specified system bars.
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        // The behavior of the immersive mode.
        val behavior = BehaviorOption.entries[behaviorSpinner.selectedItemPosition].value
        // The type of system bars to hide or show.
        val type = TypeOption.entries[typeSpinner.selectedItemPosition].value
        insetsController.systemBarsBehavior = behavior
        if (hide) {
            insetsController.hide(type)
        } else {
            insetsController.show(type)
        }
    }
}
