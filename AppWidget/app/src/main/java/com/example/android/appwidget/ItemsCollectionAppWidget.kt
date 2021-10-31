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

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.core.os.BuildCompat
import com.example.android.appwidget.ItemsCollectionRemoteViewsFactory.Companion.EXTRA_VIEW_ID
import com.example.android.appwidget.ItemsCollectionRemoteViewsFactory.Companion.REQUEST_CODE
import com.example.android.appwidget.ItemsCollectionRemoteViewsFactory.Companion.REQUEST_CODE_FROM_COLLECTION_WIDGET
import com.example.android.appwidget.ItemsCollectionRemoteViewsFactory.Companion.getRemoteCollectionItems

/**
 * Implementation of App Widget functionality that demonstrates the difference of how the list of
 * items are inflated with API level 31 and older API levels.
 * This widget also demonstrates the compound buttons (Checkbox, RadioButton, Switch), that are now
 * supported in App Widgets starting from API level 31.
 *
 * ([AppWidgetProvider] is a convenience class to aid in implementing an AppWidget provider.
 * Everything you can do with [AppWidgetProvider], you can do with a regular [BroadcastReceiver].
 * [AppWidgetProvider] merely parses the relevant fields out of the Intent that is received in
 * [onReceive], and calls hook methods with the received extras.
 */
class ItemsCollectionAppWidget : AppWidgetProvider() {

    /**
     * Called in response to the [AppWidgetManager.ACTION_APPWIDGET_UPDATE] and
     * [AppWidgetManager.ACTION_APPWIDGET_RESTORED] broadcasts when this AppWidget
     * provider is being asked to provide [RemoteViews] for a set of AppWidgets.
     * Override this method to implement your own AppWidget functionality.
     *
     * We initialize our [RemoteViews] variable `val remoteViews` to a new instance which uses
     * the layout file with the resource ID [R.layout.widget_items_collection] as the layout file
     * containing the views it will display (we pass the name of this application's package because
     * it is the package that contains the layout resource file). If [BuildCompat.isAtLeastS] returns
     * `true` indicating that the device we are running on uses a pre-release version of Android S or
     * a release version of Android S or newer we initialize our [RemoteViews.RemoteCollectionItems]
     * variable `val collectionItems` to the instance returned by our [getRemoteCollectionItems]
     * method for our [Context] parameter [context], then call the [RemoteViews.setRemoteAdapter]
     * method of `remoteViews` to set its adapter to a simple adapter for the `ListView` with the
     * ID [R.id.items_list_view] in the layout file layout/widget_items_collection.xml used by
     * `remoteViews` with `collectionItems` as the items to display in the AdapterView.
     * If [BuildCompat.isAtLeastS] returns `false` indicating that the device we are running on uses
     * a version of Android older than Android S we call the [RemoteViews.setRemoteAdapter] method
     * of `remoteViews` with [R.id.items_list_view] as the ID of the `AdapterView` and an [Intent]
     * for our [ItemsCollectionRemoteViewsService] custom [RemoteViewsService] which will be the
     * service that will be providing data to the `RemoteViewsAdapter`
     *
     * Finally no matter what version of Android our device is running we all the method
     * [AppWidgetManager.updateAppWidget] of our [appWidgetManager] parameter with our [IntArray]
     * parameter [appWidgetIds] as the `AppWidget` instances that need their RemoteViews set, and
     * our [RemoteViews] variable `remoteViews` as the RemoteViews object they should show.
     *
     * @param context The [Context] in which this receiver is running.
     * @param appWidgetManager An [AppWidgetManager] object you can use to call
     * [AppWidgetManager.updateAppWidget].
     * @param appWidgetIds The `appWidgetIds` for which an update is needed. Note that this
     * may be all of the AppWidget instances for this provider, or just a subset of them.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_items_collection)
        if (BuildCompat.isAtLeastS()) {
            val collectionItems: RemoteViews.RemoteCollectionItems =
                getRemoteCollectionItems(context)
            remoteViews.setRemoteAdapter(R.id.items_list_view, collectionItems)
        } else {
            remoteViews.setRemoteAdapter(
                R.id.items_list_view,
                Intent(context, ItemsCollectionRemoteViewsService::class.java)
            )
        }
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

    /**
     * Implements [BroadcastReceiver.onReceive] to dispatch calls to the various other methods of
     * [AppWidgetProvider]. First we call our super's implementation of `onReceive`. Then if
     * [BuildCompat.isAtLeastS] returns `true` (we are running on Android S or newer) and the
     * [Intent] parameter [intent] that we are receiving has an [REQUEST_CODE_FROM_COLLECTION_WIDGET]
     * extra stored under the key [REQUEST_CODE] we:
     *  - initialize our [Boolean] variable `val checked` to the [Boolean] stored as an extra in
     *  [intent] under the key [RemoteViews.EXTRA_CHECKED] (defaulting to `false` if there is no
     *  value stored under that key in [intent])
     *  - we toast a string constructed from the "ViewId:", the [Int] stored under the key
     *  [EXTRA_VIEW_ID] in `intent` and the current checked status: `checked`.
     *
     * @param context The [Context] in which the receiver is running.
     * @param intent The [Intent] being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (BuildCompat.isAtLeastS() &&
            intent?.extras?.getInt(REQUEST_CODE) == REQUEST_CODE_FROM_COLLECTION_WIDGET
        ) {
            val checked: Boolean? = intent.extras?.getBoolean(
                RemoteViews.EXTRA_CHECKED,
                false
            )
            Toast.makeText(
                context,
                "ViewId : ${intent.extras?.getInt(EXTRA_VIEW_ID)}'s checked status is now : $checked",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/**
 * This is the custom [RemoteViewsService] that this AppWidget uses to feed data to its
 * `RemoteViewsAdapter`. It is declared to be a `<service>` in our AndroidManifest and needs
 * the android:permission="android.permission.BIND_REMOTEVIEWS"
 */
class ItemsCollectionRemoteViewsService : RemoteViewsService() {

    /**
     * Returns an instance of our [ItemsCollectionRemoteViewsFactory] implementation of a
     * [RemoteViewsService.RemoteViewsFactory] constructed to use the [Context] of the single,
     * global [Application] object of the current process as its [Context].
     *
     * @param data The [Intent] that was used to bind to this service, as given to
     * [Context.bindService]
     * @return a [ItemsCollectionRemoteViewsFactory] that generates appropriate factories for the
     * data we provide.
     */
    override fun onGetViewFactory(data: Intent?): RemoteViewsFactory {
        return ItemsCollectionRemoteViewsFactory(applicationContext)
    }
}

/**
 * An interface for an adapter between a remote collection view (`ListView`, `GridView`, etc) and the
 * underlying data for that view. The implementor is responsible for making a RemoteView for each
 * item in the data set. This interface is a thin wrapper around Adapter.
 *
 * @param context the [Context] that we are running in.
 */
class ItemsCollectionRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    /**
     * Called when your factory is first constructed. The same factory may be shared across
     * multiple RemoteViewAdapters depending on the intent passed.
     */
    override fun onCreate() {}

    /**
     * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a
     * RemoteViewsFactory to respond to data changes by updating any internal references.
     *
     * Note: expensive tasks can be safely performed synchronously within this method. In the
     * interim, the old data will be displayed within the widget.
     *
     * See also [AppWidgetManager.notifyAppWidgetViewDataChanged]
     */
    override fun onDataSetChanged() {}

    /**
     * Called when the last RemoteViewsAdapter that is associated with this factory is unbound.
     */
    override fun onDestroy() {}

    /**
     * Returns the number of objects in our dataset. We return the number of elements in our
     * [List] of layout resource IDs [items].
     *
     * @return Count of items.
     */
    override fun getCount(): Int = items.count()

    /**
     * Get a [RemoteViews] that displays the data at the specified position in the data set. We
     * return the [RemoteViews] that our [constructRemoteViews] method builds given the layout file
     * resource ID found at index [position] of [items].
     *
     * Note: expensive tasks can be safely performed synchronously within this method, and a
     * loading view will be displayed in the interim. See [getLoadingView].
     *
     * @param position The position of the item within the Factory's data set of the item whose
     * [RemoteViews] we want.
     * @return A [RemoteViews] object corresponding to the data at the specified position.
     */
    override fun getViewAt(position: Int): RemoteViews {
        return constructRemoteViews(context, items[position])
    }

    /**
     * This allows for the use of a custom loading view which appears between the time that
     * [getViewAt] is called and returns. If `null` is returned, a default loading view will
     * be used, and we do return `null` here.
     *
     * @return The [RemoteViews] representing the desired loading view.
     */
    override fun getLoadingView(): RemoteViews? = null

    /**
     * Returns the number of types of [RemoteViews] that will be created by [getViewAt]. We return
     * the number of elements in our [List] of layout resource IDs [items].
     *
     * @return The number of types of Views that will be returned by this factory.
     */
    override fun getViewTypeCount(): Int = items.count()

    /**
     * Get the row id associated with the specified position in the list. We use the position of the
     * item within the data set as its row id so we just return that (converted to a [Long]).
     *
     * @param position The position of the item within the data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    override fun getItemId(position: Int): Long = position.toLong()

    /**
     * Indicates whether the item ids are stable across changes to the underlying data. In our case
     * they are, so we return `true`.
     *
     * @return True if the same id always refers to the same object.
     */
    override fun hasStableIds(): Boolean = true

    companion object {
        /**
         * Our dataset of layout file resource IDs, which are used to create [RemoteViews] objects
         * that will display the views contained in the layout file.
         */
        val items = listOf(
            R.layout.item_checkboxes,
            R.layout.item_radio_buttons,
            R.layout.item_switches,
        )

        /**
         * The value stored as an extra under the key [REQUEST_CODE] in the [Intent] that we receive
         * in the `onReceive` override of [ItemsCollectionAppWidget] when the [R.id.item_switch]
         * `Switch` in the [RemoteViews] constructed from layout file [R.layout.item_switches] changes
         * checked state. It is included in the [PendingIntent] that [constructRemoteViews] uses in
         * its call to [RemoteViews.setOnCheckedChangeResponse].
         */
        const val REQUEST_CODE_FROM_COLLECTION_WIDGET = 2

        /**
         * The key used to store the resource ID of the [R.id.item_switch] `Switch` in layout file
         * [R.layout.item_switches] as an extra in the [PendingIntent] that [constructRemoteViews]
         * uses in its call to [RemoteViews.setOnCheckedChangeResponse].
         */
        const val EXTRA_VIEW_ID = "extra_view_id"

        /**
         * The key used to store the value [REQUEST_CODE_FROM_COLLECTION_WIDGET] as an extra in the
         * [PendingIntent] that [constructRemoteViews] uses when it builds the [PendingIntent] for
         * the [R.id.item_switch] `Switch` in layout file [R.layout.item_switches] that will be
         * launched when the `Switch` changes state. It is used in the [PendingIntent] that
         * [constructRemoteViews] uses in its call to [RemoteViews.setOnCheckedChangeResponse].
         */
        const val REQUEST_CODE = "request_code"

        /**
         * Constructs a [RemoteViews.RemoteCollectionItems] object containing [RemoteViews] for each
         * of the layout resource IDs in our [List] of resource IDs field [items]. We initialize our
         * [RemoteViews.RemoteCollectionItems.Builder] variable to a new instance, then we use the
         * [forEachIndexed] extension function on [items] to loop through the three layout resource
         * IDs in the [List] calling the [RemoteViews.RemoteCollectionItems.Builder.addItem] method
         * of `builder` to add the [RemoteViews] that our [constructRemoteViews] method constructs
         * for the current `layoutId` using the `index` of the current entry in [items] that
         * [forEachIndexed] is supplying us as the ID to associate with the row (0-2).
         *
         * When done adding [RemoteViews] to `builder` we set the "Has Stable IDs" property of
         * `builder` to `true` (indicates that the item IDs are stable across changes to the underlying
         * data), we set the view type count of `builder` to the number of elements in [items], and
         * build `builder` into the [RemoteViews.RemoteCollectionItems] object that we return.
         *
         * @param context our application [Context] which our [constructRemoteViews] method needs to
         * construct [RemoteViews].
         * @return a [RemoteViews.RemoteCollectionItems] object whose rows contain [RemoteViews] to
         * display for each row which are constructed to display the views in the layout files whose
         * resource IDs are in our [List] of resource IDs field [items].
         */
        @RequiresApi(31)
        fun getRemoteCollectionItems(context: Context): RemoteViews.RemoteCollectionItems {
            val builder = RemoteViews.RemoteCollectionItems.Builder()
            items.forEachIndexed { index, layoutId ->
                builder.addItem(index.toLong(), constructRemoteViews(context, layoutId))
            }
            return builder.setHasStableIds(true).setViewTypeCount(items.count()).build()
        }

        /**
         * Creates a new [RemoteViews] object that will display the views contained in the layout
         * file whose resource ID is our parameter [layoutId], and if the device is running a
         * version of Android older than Android S returns that object as is. If the device is
         * running Android S or newer we branch on the value of [layoutId]:
         *  - [R.layout.item_checkboxes] (a `LinearLayout` holding a single `CheckBox`) we call the
         *  [RemoteViews.setCompoundButtonChecked] method of `remoteViews` to "Check" the `CheckBox`
         *  in the layout file whose ID is [R.id.item_checkbox].
         *  - [R.layout.item_radio_buttons] (a `RadioGroup` holding two `RadioButton` widgets) we
         *  call the [RemoteViews.setRadioGroupChecked] method of `remoteViews` to "Check" the
         *  [R.id.item_radio_button2] `RadioButton` in the [R.id.item_radio_button2] `RadioGroup`.
         *  - [R.layout.item_switches] (a `LinearLayout` holding a single `Switch` widget) we
         *  initialize our [Int] variable `val viewId` to the resource ID [R.id.item_switch] (the
         *  ID of the `Switch` in the layout file) then initialize our [PendingIntent] variable
         *  `val onCheckedChangePendingIntent` to a [PendingIntent] that will perform a broadcast
         *  using [REQUEST_CODE_FROM_COLLECTION_WIDGET] as the private request code, an [Intent]
         *  constructed to launch [ItemsCollectionAppWidget] as the [Intent] to be broadcast with
         *  two extras: `viewId` stored under the key [EXTRA_VIEW_ID] and
         *  [REQUEST_CODE_FROM_COLLECTION_WIDGET] stored under the key [REQUEST_CODE]. API level 31
         *  requires specifying either [PendingIntent.FLAG_IMMUTABLE] or [PendingIntent.FLAG_MUTABLE]
         *  so we add the flags [PendingIntent.FLAG_UPDATE_CURRENT] and [PendingIntent.FLAG_MUTABLE]
         *  to the [PendingIntent] as well. We then call the [RemoteViews.setOnCheckedChangeResponse]
         *  method of `remoteViews` with `viewId` as the ID of the view that will trigger the
         *  [PendingIntent] when checked state changes, and the [RemoteViews.RemoteResponse] that
         *  the [RemoteViews.RemoteResponse.fromPendingIntent] method creates from `onCheckedChangePendingIntent`
         *  as the [RemoteViews.RemoteResponse] to send when the checked state changes (this starts
         *  us listening for change events).
         *
         * Finally we return `remoteViews` to our caller.
         *
         * @param context the application [Context] to use to construct a [RemoteViews], [PendingIntent],
         * and [Intent].
         * @param layoutId the resource ID of the layout file whose views the [RemoteViews] object
         * that we construct should display.
         * @return a [RemoteViews] object constructed to display the views contained in the layout
         * file whose resource ID is our parameter [layoutId]. If we are running on a device that is
         * running Android S and above the [RemoteViews] object will also be configured to use some
         * of the nifty new APIs added to [RemoteViews] in Android S.
         */
        internal fun constructRemoteViews(
            context: Context,
            @LayoutRes layoutId: Int
        ): RemoteViews {
            val remoteViews = RemoteViews(context.packageName, layoutId)
            if (!BuildCompat.isAtLeastS()) {
                return remoteViews
            }
            // Compound buttons in a widget are stateless. You need to change the state and register for
            // the state change events.
            when (layoutId) {
                R.layout.item_checkboxes -> {
                    // This code will check the Checkbox
                    remoteViews.setCompoundButtonChecked(R.id.item_checkbox, true)
                }
                R.layout.item_radio_buttons -> {
                    // This code will check the item_radio_button2 in the item_radio_group RadioGroup
                    remoteViews.setRadioGroupChecked(
                        R.id.item_radio_group,
                        R.id.item_radio_button2
                    )
                }
                R.layout.item_switches -> {
                    val viewId: Int = R.id.item_switch
                    val onCheckedChangePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE_FROM_COLLECTION_WIDGET,
                        Intent(context, ItemsCollectionAppWidget::class.java).apply {
                            putExtra(EXTRA_VIEW_ID, viewId)
                            putExtra(REQUEST_CODE, REQUEST_CODE_FROM_COLLECTION_WIDGET)
                        },
                        // API level 31 requires specifying either of
                        // PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    // Listen for change events.
                    // RemoteResponse.fromPendingIntent works on an individual item whereas you can set
                    // a PendingIntent template using RemoteViews.setPendingIntentTemplate and
                    // distinguish individual on-click by calling RemoteResponse.fromFillInIntent.
                    // See
                    // https://developer.android.com/reference/android/widget/RemoteViews.RemoteResponse#fromPendingIntent(android.app.PendingIntent)
                    // https://developer.android.com/reference/android/widget/RemoteViews.RemoteResponse#fromFillInIntent(android.content.Intent)
                    // for more details.
                    remoteViews.setOnCheckedChangeResponse(
                        viewId,
                        RemoteViews.RemoteResponse.fromPendingIntent(
                            onCheckedChangePendingIntent
                        )
                    )
                }
            }
            return remoteViews
        }
    }
}