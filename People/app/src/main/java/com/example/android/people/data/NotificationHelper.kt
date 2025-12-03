/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.people.data

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.BUBBLE_PREFERENCE_NONE
import android.app.PendingIntent
import android.app.Person
import android.app.RemoteInput
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.example.android.people.BubbleActivity
import com.example.android.people.MainActivity
import com.example.android.people.R
import com.example.android.people.ReplyReceiver

/**
 * Handles all operations related to [Notification].
 *
 * @param context The application context.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        /**
         * The notification channel for messages. This is used for showing Bubbles.
         */
        private const val CHANNEL_NEW_MESSAGES = "new_messages"

        /**
         * The request code for the [PendingIntent] of the "Open Content" action on the expanded bubble,
         * as well as the fall-back notification.
         */
        private const val REQUEST_CONTENT = 1

        /**
         * The request code for the [PendingIntent] of the "Reply" action in the expanded bubble.
         */
        private const val REQUEST_BUBBLE = 2
    }

    /**
     * The [NotificationManager] for the app. This is used to create notification channels and post
     * notifications.
     */
    private val notificationManager: NotificationManager =
        context.getSystemService() ?: throw IllegalStateException()

    /**
     * The [ShortcutManager] for the app. This is used to update dynamic shortcuts when needed.
     */
    private val shortcutManager: ShortcutManager =
        context.getSystemService() ?: throw IllegalStateException()

    /**
     * Creates the notification channel for new messages.
     * This is required for all notifications on Android 8.0 (API level 26) and above.
     * The importance must be IMPORTANCE_HIGH to be eligible for Bubbles.
     * This method also initializes the dynamic shortcuts, which are used for conversations.
     */
    fun setUpNotificationChannels() {
        if (notificationManager.getNotificationChannel(CHANNEL_NEW_MESSAGES) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_NEW_MESSAGES,
                    context.getString(R.string.channel_new_messages),
                    // The importance must be IMPORTANCE_HIGH to show Bubbles.
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_new_messages_description)
                }
            )
        }
        updateShortcuts(null)
    }

    /**
     * Updates the dynamic shortcuts for all contacts.
     *
     * If an `importantContact` is provided, it will be moved to the top of the list. The list of
     * shortcuts is then truncated to the maximum number of shortcuts allowed by the system.
     * Finally, the updated list of shortcuts is published to the `ShortcutManager`.
     *
     * These shortcuts are used as conversation shortcuts and are required for Bubbles.
     *
     * @param importantContact The contact to be prioritized in the shortcut list. Can be null.
     */
    @SuppressLint("ReportShortcutUsage")
    @WorkerThread
    fun updateShortcuts(importantContact: Contact?) {
        var shortcuts: List<ShortcutInfo> = Contact.CONTACTS.map { contact: Contact ->
            val icon: Icon = Icon.createWithAdaptiveBitmap(
                context.resources.assets.open(contact.icon).use { input ->
                    BitmapFactory.decodeStream(input)
                }
            )
            // Create a dynamic shortcut for each of the contacts.
            // The same shortcut ID will be used when we show a bubble notification.
            ShortcutInfo.Builder(context, contact.shortcutId)
                .setLocusId(LocusId(contact.shortcutId))
                .setActivity(ComponentName(context, MainActivity::class.java))
                .setShortLabel(contact.name)
                .setIcon(icon)
                .setLongLived(true)
                .setCategories(setOf("com.example.android.bubbles.category.TEXT_SHARE_TARGET"))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(
                            "https://android.example.com/chat/${contact.id}".toUri()
                        )
                )
                .setPerson(
                    Person.Builder()
                        .setName(contact.name)
                        .setIcon(icon)
                        .build()
                )
                .build()
        }
        // Move the important contact to the front of the shortcut list.
        if (importantContact != null) {
            shortcuts = shortcuts.sortedByDescending { it.id == importantContact.shortcutId }
        }
        // Truncate the list if we can't show all of our contacts.
        val maxCount = shortcutManager.maxShortcutCountPerActivity
        if (shortcuts.size > maxCount) {
            shortcuts = shortcuts.take(maxCount)
        }
        shortcutManager.addDynamicShortcuts(shortcuts)
    }

    /**
     * Shows a notification for a chat message.
     *
     * This method constructs and displays a notification that can be promoted to a bubble.
     * It configures the bubble metadata, such as the intent for the expanded view, the desired
     * height, and behavior based on user interaction. The notification is built with a
     * `MessagingStyle` to display the conversation history, and includes a direct reply action.
     *
     * The notification is associated with a dynamic shortcut, which is a prerequisite for Bubbles.
     * It also handles different behaviors for new messages versus updates to existing ones.
     *
     * @param chat The chat data, including the contact and messages.
     * @param fromUser `true` if the notification is being triggered by a direct user action (e.g.,
     * tapping a shortcut), which can cause the bubble to auto-expand. `false` if it's for an
     * incoming message.
     * @param update `true` if this is an update to an existing notification. This suppresses the
     * notification shade entry and prevents re-alerting the user with sound or vibration.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @WorkerThread
    fun showNotification(chat: Chat, fromUser: Boolean, update: Boolean = false) {
        updateShortcuts(chat.contact)
        val icon: Icon = Icon.createWithAdaptiveBitmapContentUri(chat.contact.iconUri)
        val user: Person = Person.Builder().setName(context.getString(R.string.sender_you)).build()
        val person: Person = Person.Builder().setName(chat.contact.name).setIcon(icon).build()
        val contentUri: Uri = "https://android.example.com/chat/${chat.contact.id}".toUri()

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            // Launch BubbleActivity as the expanded bubble.
            Intent(context, BubbleActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(contentUri),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = Notification.Builder(context, CHANNEL_NEW_MESSAGES)
            // A notification can be shown as a bubble by calling setBubbleMetadata()
            .setBubbleMetadata(
                Notification.BubbleMetadata.Builder(pendingIntent, icon)
                    // The height of the expanded bubble.
                    .setDesiredHeight(context.resources.getDimensionPixelSize(R.dimen.bubble_height))
                    .apply {
                        // When the bubble is explicitly opened by the user, we can show the bubble
                        // automatically in the expanded state. This works only when the app is in
                        // the foreground.
                        if (fromUser) {
                            setAutoExpandBubble(true)
                        }
                        if (fromUser || update) {
                            setSuppressNotification(true)
                        }
                    }
                    .build()
            )
            // The user can turn off the bubble in system settings. In that case, this notification
            // is shown as a normal notification instead of a bubble. Make sure that this
            // notification works as a normal notification as well.
            .setContentTitle(chat.contact.name)
            .setSmallIcon(R.drawable.ic_message)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setShortcutId(chat.contact.shortcutId)
            // This ID helps the intelligence services of the device to correlate this notification
            // with the corresponding dynamic shortcut.
            .setLocusId(LocusId(chat.contact.shortcutId))
            .addPerson(person)
            .setShowWhen(true)
            // The content Intent is used when the user clicks on the "Open Content" icon button on
            // the expanded bubble, as well as when the fall-back notification is clicked.
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            )
            // Direct Reply
            .addAction(
                Notification.Action
                    .Builder(
                        Icon.createWithResource(context, R.drawable.ic_send),
                        context.getString(R.string.label_reply),
                        PendingIntent.getBroadcast(
                            context,
                            REQUEST_CONTENT,
                            Intent(context, ReplyReceiver::class.java).setData(contentUri),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                        )
                    )
                    .addRemoteInput(
                        RemoteInput.Builder(ReplyReceiver.KEY_TEXT_REPLY)
                            .setLabel(context.getString(R.string.hint_input))
                            .build()
                    )
                    .setAllowGeneratedReplies(true)
                    .build()
            )
            // Let's add some more content to the notification in case it falls back to a normal
            // notification.
            .setStyle(
                Notification.MessagingStyle(user)
                    .apply {
                        val lastId = chat.messages.last().id
                        for (message in chat.messages) {
                            val m = Notification.MessagingStyle.Message(
                                message.text,
                                message.timestamp,
                                if (message.isIncoming) person else null
                            ).apply {
                                if (message.photoUri != null) {
                                    setData(message.photoMimeType, message.photoUri)
                                }
                            }
                            if (message.id < lastId) {
                                addHistoricMessage(m)
                            } else {
                                addMessage(m)
                            }
                        }
                    }
                    .setGroupConversation(false)
            )
            .setWhen(chat.messages.last().timestamp)
        // Don't sound/vibrate if an update to an existing notification.
        if (update) {
            builder.setOnlyAlertOnce(true)
        }
        notificationManager.notify(chat.contact.id.toInt(), builder.build())
    }

    /**
     * Dismisses a notification.
     *
     * @param id The ID of the notification to be dismissed.
     */
    private fun dismissNotification(id: Long) {
        notificationManager.cancel(id.toInt())
    }

    /**
     * Checks whether the app can show a bubble for a specific conversation.
     *
     * Bubbles are enabled if the user has not disabled them in the system settings and if the
     * notification channel for the conversation is configured to allow bubbling. This check is
     * version-dependent. On Android S (API 31) and higher, it checks the global `bubblePreference`.
     * On older versions, it uses the deprecated `areBubblesAllowed()` method.
     *
     * @param contact The contact for which to check the bubble eligibility.
     * @return `true` if a bubble can be shown for this contact, `false` otherwise.
     */
    fun canBubble(contact: Contact): Boolean {
        val channel = notificationManager.getNotificationChannel(
            CHANNEL_NEW_MESSAGES,
            contact.shortcutId
        )
        val areBubblesPreferred: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationManager.bubblePreference != BUBBLE_PREFERENCE_NONE
        } else {
            @Suppress("DEPRECATION") // Needed for Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            notificationManager.areBubblesAllowed()
        }
        return areBubblesPreferred || channel?.canBubble() == true
    }

    /**
     * Updates or dismisses the notification for a chat.
     *
     * This function is called when the user enters a chat screen.
     *
     * If the chat screen is launched with pre-populated messages from the notification, it means the
     * user has seen the messages, so the notification is dismissed.
     *
     * If the user enters the chat through other means (e.g., from the conversation list), the
     * notification is updated to remove the unread message badge on the collapsed bubble. This is
     * done by re-issuing the notification with the `suppressNotification` flag set, which removes
     * the visual indicator that there's a new message without dismissing the bubble itself.
     *
     * @param chat The chat data used for updating the notification.
     * @param chatId The ID of the chat, used for dismissing the notification.
     * @param prepopulatedMsgs `true` if the chat messages were pre-populated from a notification,
     * indicating the notification should be dismissed. `false` otherwise, indicating the
     * notification should just be updated to remove the unread badge.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun updateNotification(chat: Chat, chatId: Long, prepopulatedMsgs: Boolean) {
        if (!prepopulatedMsgs) {
            // Update notification bubble metadata to suppress notification so that the unread
            // message badge icon on the collapsed bubble is removed.
            showNotification(chat, fromUser = false, update = true)
        } else {
            dismissNotification(chatId)
        }
    }
}
