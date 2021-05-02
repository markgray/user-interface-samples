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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Behaviors of immersive mode. The [title] field is used as the items to be displayed by the
 * [ArrayAdapter] used as the `adapter` of the [Spinner] field `behaviorSpinner` (resource ID
 * [R.id.behavior] in our layout file), and the [Int] field [value] is the behavior constant that
 * the `systemBarsBehavior` is to be set to in the `controlWindowInsets` method.
 */
enum class BehaviorOption(
    val title: String,
    val value: Int
) {
    /**
     * Swipe from the edge to show a hidden bar.
     */
    ShowBarsBySwipe(
        "BEHAVIOR_SHOW_BARS_BY_SWIPE",
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    ),

    /**
     * Any interaction on the display to show the navigation bar.
     */
    ShowBarsByTouch(
        "BEHAVIOR_SHOW_BARS_BY_TOUCH",
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
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
 * [ArrayAdapter] used as the `adapter` of the [Spinner] field `TypeOption` (resource ID [R.id.type]
 * in our layout file), and the [Int] field [value] is the type constant that is passed to the
 * [WindowInsetsControllerCompat] `hide` or `show` method in the `controlWindowInsets` method.
 */
enum class TypeOption(
    val title: String,
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
     * The [Spinner] in our layout file with ID [R.id.behavior] (labeled "Behavior") used to select
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
     * The [Spinner] in our layout file with ID [R.id.type] (labeled "Type") used to select which of
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
     * Called when the activity is starting.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        behaviorSpinner = findViewById(R.id.behavior)
        behaviorSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            BehaviorOption.values().map { it.title }
        )

        typeSpinner = findViewById(R.id.type)
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            TypeOption.values().map { it.title }
        )

        val hideButton: Button = findViewById(R.id.hide)
        hideButton.setOnClickListener { controlWindowInsets(true) }
        val showButton: Button = findViewById(R.id.show)
        showButton.setOnClickListener { controlWindowInsets(false) }
    }

    private fun controlWindowInsets(hide: Boolean) {
        // WindowInsetsController can hide or show specified system bars.
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        // The behavior of the immersive mode.
        val behavior = BehaviorOption.values()[behaviorSpinner.selectedItemPosition].value
        // The type of system bars to hide or show.
        val type = TypeOption.values()[typeSpinner.selectedItemPosition].value
        insetsController.systemBarsBehavior = behavior
        if (hide) {
            insetsController.hide(type)
        } else {
            insetsController.show(type)
        }
    }
}
