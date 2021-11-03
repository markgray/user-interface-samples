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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.SizeF
import android.widget.RemoteViews

/**
 * Implementation of the weather forecast app widget that demonstrates the flexible layouts based
 * on the size of the device.
 */
class WeatherForecastAppWidget : AppWidgetProvider() {

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
     * parameter [appWidgetIds] calling our [updateWeatherWidget] method with our [Context] parameter
     * [context], our [AppWidgetManager] parameter [appWidgetManager], and the current `appWidgetId`.
     * [updateWeatherWidget] will create, configure and update the [RemoteViews] for the app widget
     * ID `appWidgetId` based on the layout file whose resource ID is stored for that `appWidgetId`
     * in our shared preferences file.
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
        for (appWidgetId in appWidgetIds) {
            updateWeatherWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Constructs a [RemoteViews] that will display the views for a "weather forecast app widget",
     * then has our [AppWidgetManager] parameter [appWidgetManager] set the [RemoteViews] to use for
     * our app Widget ID parameter [appWidgetId] to this [RemoteViews]. This [RemoteViews] is chosen
     * by [appWidgetManager] from a [Map] of [SizeF] to [RemoteViews] based on the closest size
     * specification. We initialize our [Map] of [SizeF] to [RemoteViews] variable `val viewMapping`
     * to an instance with three entries, one mapping a 180.0f by 110.0f [SizeF] to a [RemoteViews]
     * constructed to display the views in the layout file with resource ID
     * [R.layout.widget_weather_forecast_small], one mapping a 270.0f by 110.0f [SizeF] to a
     * [RemoteViews] constructed to display the views in the layout file with resource ID
     * [R.layout.widget_weather_forecast_medium], and one mapping a 270.0f by 280.0f [SizeF] to a
     * [RemoteViews] constructed to display the views in the layout file with resource ID
     * [R.layout.widget_weather_forecast_large]. We then call the [AppWidgetManager.updateAppWidget]
     * method of [appWidgetManager] to have it set the [RemoteViews] to use for the app widget ID
     * [appWidgetId] to a [RemoteViews] constructed with `viewMapping` providing the choice of
     * [RemoteViews] to use depending on the closest size specification that will fit in `appWidgetId`.
     *
     * @param context the [Context] in which this receiver is running.
     * @param appWidgetManager the [AppWidgetManager] instance to use for the supplied [Context]
     * object.
     * @param appWidgetId a unique ID that each [RemoteViews] instance is assigned at the time
     * of binding. It is one of the app widget IDs that is passed to our [onUpdate] override in
     * its [IntArray] parameter `appWidgetIds`.
     */
    private fun updateWeatherWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            // Specify the minimum width and height in dp and a layout, which you want to use for the
            // specified size
            // In the following case:
            //   - R.layout.widget_weather_forecast_small is used from
            //     180dp (or minResizeWidth) x 110dp (or minResizeHeight) to 269dp (next cutoff point - 1) x 279dp (next cutoff point - 1)
            //   - R.layout.widget_weather_forecast_medium is used from 270dp x 110dp to 270dp x 279dp (next cutoff point - 1)
            //   - R.layout.widget_weather_forecast_large is used from
            //     270dp x 280dp to 570dp (specified as maxResizeWidth) x 450dp (specified as maxResizeHeight)
            SizeF(180.0f, 110.0f) to RemoteViews(
                context.packageName,
                R.layout.widget_weather_forecast_small
            ),
            SizeF(270.0f, 110.0f) to RemoteViews(
                context.packageName,
                R.layout.widget_weather_forecast_medium
            ),
            SizeF(270.0f, 280.0f) to RemoteViews(
                context.packageName,
                R.layout.widget_weather_forecast_large
            )
        )
        appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(viewMapping))
    }
}

