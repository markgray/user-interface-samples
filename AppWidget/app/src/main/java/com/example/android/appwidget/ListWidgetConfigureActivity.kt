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
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
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
     * Called when the activity is starting. First we call our super's implementation of `onCreate`.
     * We then set our result to [Activity.RESULT_CANCELED] (This will cause the widget host to
     * cancel out of the widget placement if the user presses the back button). Next we initialize
     * our [ActivityWidgetConfigureBinding] variable `val binding` to the instance that the
     * [ActivityWidgetConfigureBinding.inflate] method inflates and binds to when it uses the
     * [LayoutInflater] instance that this Window retrieved from its Context to inflate its
     * associated layout file layout/activity_widget_configure.xml (resource ID
     * [R.layout.activity_widget_configure]). This layout file consists of a vertical `LinearLayout`
     * root view which holds two vertical `LinearLayout`'s which hold a "Grocery List" and a
     * "To-do List" which consist of a bunch of `CheckBox` widgets for items on the list. Having
     * inflated our [ActivityWidgetConfigureBinding] we set our content view to the outermost `View`
     * in the layout file associated [ActivityWidgetConfigureBinding], and then set the title
     * associated with this activity to the string "Select list for widgets".
     *
     * We next set the [View.OnClickListener] of the [ActivityWidgetConfigureBinding.groceryListContainer]
     * `LinearLayout` which holds the "Grocery List" to a lambda which calls our [onWidgetContainerClicked]
     * method with the resource ID [R.layout.widget_grocery_list] which refers to the layout file
     * layout/widget_grocery_list.xml (which consists of a bunch of `CheckBox` widgets whose
     * android:text attributes are names of grocery items).
     *
     * Then we set the [View.OnClickListener] of the [ActivityWidgetConfigureBinding.todoListContainer]
     * `LinearLayout` which holds the "To-do List" to a lambda which calls our [onWidgetContainerClicked]
     * method with the resource ID [R.layout.widget_todo_list] which refers to the layout file
     * layout/widget_todo_list.xml (which consists of a bunch of `CheckBox` widgets whose
     * android:text attributes are names of household chores).
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
     * @param icicle we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val binding = ActivityWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
