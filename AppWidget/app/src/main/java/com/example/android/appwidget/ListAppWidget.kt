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
                val viewMapping = mapOf(
                    SizeF(150f, 150f) to constructRemoteViews(
                        R.layout.widget_grocery_list
                    ), SizeF(250f, 150f) to constructRemoteViews(
                        R.layout.widget_grocery_grid
                    )
                )
                RemoteViews(viewMapping)
            } else {
                constructRemoteViews(
                    layoutId
                )
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}
