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

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.SizeF
import android.widget.RemoteViews
import androidx.annotation.LayoutRes

/**
 * Implementation of a list app widget.
 */
class ListAppWidget : AppWidgetProvider() {

    /**
     * Called in response to the [AppWidgetManager.ACTION_APPWIDGET_UPDATE] broadcast (sent when it
     * is time to update your `AppWidget` which may be sent in response to a new instance for this
     * `AppWidget` provider having been instantiated, the requested update interval having lapsed,
     * or the system booting) and [AppWidgetManager.ACTION_APPWIDGET_RESTORED] broadcast (sent to an
     * [AppWidgetProvider] after `AppWidget` state related to that provider has been restored from
     * backup) when this [AppWidgetManager] is being asked to provide [RemoteViews] for a set of
     * `AppWidgets`. Override this method to implement your own `AppWidget` functionality.
     *
     * We loop over [Int] variable `appWidgetId` for all of the `appWidgetId` in our [IntArray]
     * parameter [appWidgetIds] calling our [updateAppWidget] method with our [Context] parameter
     * [context], our [AppWidgetManager] parameter [appWidgetManager], and the current `appWidgetId`.
     * [updateAppWidget] will create, configure and update the [RemoteViews] for the app widget ID
     * `appWidgetId` based on the layout file whose resource ID is stored for that `appWidgetId` in
     * our shared preferences file.
     *
     * @param context The [Context] in which this receiver is running.
     * @param appWidgetManager A [AppWidgetManager] object you can call
     * [AppWidgetManager.updateAppWidget] on.
     * @param appWidgetIds The `appWidgetIds` for which an update is needed. Note that this may be
     * all of the `AppWidget` instances for this provider, or just a subset of them. An `appWidgetId`
     * is a unique ID that each [RemoteViews] instance is assigned at the time of binding. This ID
     * is persistent across the lifetime of the widget, that is, until it is deleted from the
     * `AppWidgetHost`.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId: Int in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Called in response to the [AppWidgetManager.ACTION_APPWIDGET_DELETED] broadcast which is sent
     * when one or more `AppWidget` instances have been deleted from their host. Override this
     * method to implement your own `AppWidget` functionality. We loop over [Int] variable
     * `appWidgetId` for all of the `appWidgetId` in our [IntArray] parameter [appWidgetIds] calling
     * our [ListSharedPrefsUtil.deleteWidgetLayoutIdPref] method with our [Context] parameter
     * [context], and the current `appWidgetId`. [ListSharedPrefsUtil.deleteWidgetLayoutIdPref]
     * deletes the layout file resource ID that is stored in our shared preferences file for
     * `appWidgetId` (thereby preventing it from being recreated in our [onUpdate] override.
     *
     * @param context The [Context] in which this receiver is running.
     * @param appWidgetIds The `appWidgetIds` that have been deleted from their host.
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            ListSharedPrefsUtil.deleteWidgetLayoutIdPref(context, appWidgetId)
        }
    }

    companion object {

        /**
         * Private request code used for the [PendingIntent] which is launched when the user clicks
         * on one of our 'AppWidget`'s
         */
        private const val REQUEST_CODE_OPEN_ACTIVITY = 1

        /**
         * Constructs a [RemoteViews] that will display the views in the current layout file whose
         * resource ID is stored in our shared preferences file under a key which is associated with
         * our [Int] parameter [appWidgetId] then has our [AppWidgetManager] parameter
         * [appWidgetManager] set the [RemoteViews] to use for the specified [appWidgetId] to this
         * [RemoteViews]. First we initialize our [Intent] variable `val activityIntent` with a new
         * instance intended to launch our [MainActivity] class, and using the [apply] extension
         * function on the new instance set the special flags controlling how this intent is handled
         * to [Intent.FLAG_ACTIVITY_CLEAR_TASK] (if set in an Intent passed to [Context.startActivity],
         * this flag will cause any existing task that would be associated with the activity to be
         * cleared before the activity is started), and [Intent.FLAG_ACTIVITY_NEW_TASK] (if set,
         * this activity will become the start of a new task on this history stack). Next we
         * initalize our [PendingIntent] variable `val appOpenIntent` with a [PendingIntent] that
         * will start a new activity using our [Context] parameter [context] as the [Context] in
         * which this [PendingIntent] should start the activity, [REQUEST_CODE_OPEN_ACTIVITY] as the
         * private request code for the sender, `activityIntent` as the [Intent] of the activity to
         * be launched, and the flags [PendingIntent.FLAG_CANCEL_CURRENT] (flag indicating that if
         * the described PendingIntent already exists, the current one should be canceled before
         * generating a new one), and [PendingIntent.FLAG_IMMUTABLE] (flag indicating that the
         * created PendingIntent should be immutable, this means that the additional intent argument
         * passed to the send methods to fill in unpopulated properties of this intent will be
         * ignored) as the flags of the [PendingIntent]. Next we initialize our [Int] variable
         * `val layoutId` to the layout resource ID that the [ListSharedPrefsUtil.loadWidgetLayoutIdPref]
         * method retrieves from our shared preference for the app widget ID `appWidgetId`. Then
         * we initialize our [RemoteViews] variable `val remoteViews` depending on the value of
         * `layoutId`:
         *  - `layoutId` is [R.layout.widget_grocery_list]: we initialize our [Map] of [SizeF] to
         *  [RemoteViews] variable `val viewMapping` to an instance with two entries, one mapping a
         *  150f by 150f [SizeF] to the [RemoteViews] that our `constructRemoteViews` method constructs
         *  to display the views in the layout file whose resource ID is [R.layout.widget_grocery_list]
         *  and one mapping a 250f by 150f [SizeF] to the [RemoteViews] that `constructRemoteViews`
         *  constructs to display the views in the layout file whose resource ID is
         *  [R.layout.widget_grocery_grid]. We then "return" a [RemoteViews] instance constructed to
         *  inflate the layout with the closest size specification in `viewMapping` as the value
         *  to be assigned to `remoteViews`.
         *  - any other value of `layoutId` just returns a [RemoteViews] constructed to display the
         *  views in the layout file whose resource ID is `layoutId` as the value to be assigned to
         *  `remoteViews`.
         *
         * Finally we call the [AppWidgetManager.updateAppWidget] method of [appWidgetManager] to
         * have it set the [RemoteViews] to use for the [appWidgetId] app widget ID.
         *
         * @param context the [Context] in which this receiver is running.
         * @param appWidgetManager the [AppWidgetManager] instance to use for the supplied [Context]
         * object.
         * @param appWidgetId a unique ID that each [RemoteViews] instance is assigned at the time
         * of binding. It is one of the app widget IDs that is passed to our [onUpdate] override in
         * its [IntArray] parameter `appWidgetIds`.
         */
        @SuppressLint("RemoteViewLayout")
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val appOpenIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_OPEN_ACTIVITY,
                activityIntent,
                // API level 31 requires specifying either of
                // PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE
                // See https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            /**
             * Nested function called from [updateAppWidget] to construct a [RemoteViews] that will
             * display the views in the layout file whose resource ID is our [Int] parameter
             * `widgetLayoutId`
             *
             * @param widgetLayoutId the resource ID of the layout file whose views the [RemoteViews]
             * object we construct and return are supposed to display.
             * @return a [RemoteViews] constructed to display the views in the layout file whose
             * resource ID is our [Int] parameter [widgetLayoutId].
             */
            fun constructRemoteViews(
                @LayoutRes widgetLayoutId: Int
            ) = RemoteViews(context.packageName, widgetLayoutId).apply {
                if (widgetLayoutId == R.layout.widget_grocery_list ||
                    widgetLayoutId == R.layout.widget_grocery_grid
                ) {
                    setTextViewText(
                        R.id.checkbox_list_title,
                        context.resources.getText(R.string.grocery_list)
                    )
                } else if (widgetLayoutId == R.layout.widget_todo_list) {
                    setTextViewText(
                        R.id.checkbox_list_title,
                        context.resources.getText(R.string.todo_list)
                    )
                }
                setOnClickPendingIntent(R.id.checkbox_list_title, appOpenIntent)
            }

            val layoutId = ListSharedPrefsUtil.loadWidgetLayoutIdPref(context, appWidgetId)
            val remoteViews = if (layoutId == R.layout.widget_grocery_list) {
                // Specify the maximum width and height in dp and a layout, which you want to use
                // for the specified size
                val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                    SizeF(150f, 150f) to constructRemoteViews(
                        R.layout.widget_grocery_list
                    ), SizeF(250f, 150f) to constructRemoteViews(
                        R.layout.widget_grocery_grid
                    )
                )
                RemoteViews(viewMapping)
            } else {
                constructRemoteViews(layoutId)
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}
