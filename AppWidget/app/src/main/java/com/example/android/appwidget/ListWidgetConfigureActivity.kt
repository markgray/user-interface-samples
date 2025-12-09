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

package com.example.android.appwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.android.appwidget.databinding.ActivityWidgetConfigureBinding

/**
 * The configuration screen for the [ListAppWidget] widget. Its has an `<intent-filter>` in the
 * AndroidManifest.xml whose action name is "android.appwidget.action.APPWIDGET_UPDATE" and is
 * the value of an `android:configure` attribute of the `<appwidget-provider>` file
 * xml/app_widget_info_checkbox_list.xml, and this file is referenced in a `<meta-data>` attribute
 * as the android:name="android.appwidget.provider" for the `<receiver>` [ListAppWidget].
 * An "android.appwidget.action.APPWIDGET_UPDATE" broadcast Intent is sent when it is time to update
 * your AppWidget. The android:configure attribute names this Activity as the configuration activity
 * of [ListAppWidget].
 */
class ListWidgetConfigureActivity : AppCompatActivity() {

    /**
     * The `appWidgetId` of the widget we are to configure that the AppWidget manager sent us in the
     * [AppWidgetManager.EXTRA_APPWIDGET_ID] extra of the [Intent] that launched us. We set it in
     * our [onCreate] override.  [AppWidgetManager.INVALID_APPWIDGET_ID] is a sentinel value that
     * the AppWidget manager will never return as a `appWidgetId` so we use it as the value until we
     * know which app widget we are to configure. The [ListAppWidget.updateAppWidget] method uses
     * this `appWidgetId` when it calls the [AppWidgetManager.updateAppWidget] method to set the
     * [RemoteViews] to use for the specified appWidgetId.
     */
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`. We then set our result
     * to [Activity.RESULT_CANCELED] (This will cause the widget host to cancel out of the widget
     * placement if the user presses the back button). Next we initialize our
     * [ActivityWidgetConfigureBinding] variable `val binding` to the instance that the
     * [ActivityWidgetConfigureBinding.inflate] method inflates and binds to when it uses the
     * [LayoutInflater] instance that this Window retrieved from its Context to inflate its
     * associated layout file layout/activity_widget_configure.xml (resource ID
     * `R.layout.activity_widget_configure`). This layout file consists of a vertical `LinearLayout`
     * root view which holds two vertical `LinearLayout`'s which hold a "Grocery List" and a
     * "To-do List" which consist of a bunch of `CheckBox` widgets for items on the list. Having
     * inflated our [ActivityWidgetConfigureBinding] we set our content view to the outermost `View`
     * in the layout file associated [ActivityWidgetConfigureBinding], and then set the title
     * associated with this activity to the string "Select list for widgets".
     *
     * We call [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy for applying
     * window insets to the `binding.root` root [View], with the `listener`
     * argument a lambda that accepts the [View] passed the lambda
     * in variable `v` and the [WindowInsetsCompat] passed the lambda
     * in variable `windowInsets`. It initializes its [Insets] variable
     * `systemBars` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument. It then gets the insets for the
     * IME (keyboard) using [WindowInsetsCompat.Type.ime]. It then updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `systemBars.left`, the right margin set to
     * `systemBars.right`, the top margin set to `systemBars.top`, and the bottom margin
     * set to the maximum of the system bars bottom inset and the IME bottom inset.
     * Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * We next set the [View.OnClickListener] of the
     * [ActivityWidgetConfigureBinding.groceryListContainer] `LinearLayout` which holds the
     * "Grocery List" to a lambda which calls our [onWidgetContainerClicked] method with the
     * resource ID `R.layout.widget_grocery_list` which refers to the layout file
     * layout/widget_grocery_list.xml (which consists of a bunch of `CheckBox` widgets whose
     * android:text attributes are names of grocery items).
     *
     * Then we set the [View.OnClickListener] of the
     * [ActivityWidgetConfigureBinding.todoListContainer] `LinearLayout` which holds the
     * "To-do List" to a lambda which calls our [onWidgetContainerClicked] method with the resource
     * ID `R.layout.widget_todo_list` which refers to the layout file layout/widget_todo_list.xml
     * (which consists of a bunch of `CheckBox` widgets whose android:text attributes are names of household chores).
     *
     * We next initialize our [Int] field [appWidgetId] to the value stored in the [Intent] that
     * launched us under the key [AppWidgetManager.EXTRA_APPWIDGET_ID], defaulting to the value
     * [AppWidgetManager.INVALID_APPWIDGET_ID] if there is not such key. If [appWidgetId] is
     * [AppWidgetManager.INVALID_APPWIDGET_ID] this activity was started with an intent without an
     * app widget ID so we finish with an error by calling the [finish] method. Otherwise we just
     * hang around until the user clicks the [ActivityWidgetConfigureBinding.groceryListContainer]
     * or [ActivityWidgetConfigureBinding.todoListContainer] which will then call our
     * [onWidgetContainerClicked] method with the appropriate layout resource ID for the
     * [ListAppWidget] to use in its role as a [AppWidgetProvider].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val binding = ActivityWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = systemBars.left
                rightMargin = systemBars.right
                topMargin = systemBars.top
                bottomMargin = systemBars.bottom.coerceAtLeast(ime.bottom)
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        title = getString(R.string.select_list_for_widget)

        binding.groceryListContainer.setOnClickListener {
            onWidgetContainerClicked(R.layout.widget_grocery_list)
        }
        binding.todoListContainer.setOnClickListener {
            onWidgetContainerClicked(R.layout.widget_todo_list)
        }

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    /**
     * This is the [View.OnClickListener] that is called when the user clicks either `LinearLayout`
     * [ActivityWidgetConfigureBinding.groceryListContainer] or `LinearLayout`
     * [ActivityWidgetConfigureBinding.todoListContainer] to select the layout used by the
     * [ListAppWidget] whose `appWidgetId` is our [appWidgetId] field (the `LinearLayout`'s
     * hold non-functional representations of what a "Grocery list" or a "To-do list" will look
     * like when [ListAppWidget] uses our [widgetLayoutResId] `LayoutRes` parameter as the layout
     * file for the `AppWidget`).
     *
     * First we call our [ListSharedPrefsUtil.saveWidgetLayoutIdPref] method to have it store our
     * [widgetLayoutResId] parameter in our shared preferences file under a key generated from our
     * `appWidgetId` field [appWidgetId]. Next we initialize our [AppWidgetManager] variable
     * `val appWidgetManager` to the [AppWidgetManager] instance to use for our [Context]. Then we
     * call the [ListAppWidget.updateAppWidget] method with `this` as the [Context] to use when
     * a [Context] is needed, `appWidgetManager` as the [AppWidgetManager] to interact with and
     * [appWidgetId] as the `appWidgetId` of the [ListAppWidget] that it should update to use the
     * new layout file whose resource ID is [widgetLayoutResId].
     *
     * Next we initialize our [Intent] variable `val resultValue` to a new instance, and add
     * [appWidgetId] to it as an extra under the key [AppWidgetManager.EXTRA_APPWIDGET_ID]
     * (we do this to pass back the original appWidgetId so that the result we return can be
     * properly identified). Then we set the result that our activity will return to its caller
     * to the `resultCode` [Activity.RESULT_OK] with `resultValue` as the data to propagate back to
     * the originating activity. Finally we call [finish] to close our activity.
     */
    private fun onWidgetContainerClicked(@LayoutRes widgetLayoutResId: Int) {
        ListSharedPrefsUtil.saveWidgetLayoutIdPref(this, appWidgetId, widgetLayoutResId)
        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        ListAppWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}
