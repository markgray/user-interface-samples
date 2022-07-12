/*
 * Copyright 2018 The Android Open Source Project
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
package com.example.android.interactivesliceprovider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.appindexing.FirebaseAppIndex

/**
 * Note: Firebase App Indexing is no longer the recommended way of indexing content for display as
 * suggested results in Google Search App. https://firebase.google.com/docs/app-indexing points to
 * other useful Google developer products.
 */
class AppIndexingUpdateReceiver : BroadcastReceiver() {
    /**
     * This method is called when the [BroadcastReceiver] is receiving an [Intent] broadcast.
     * If the [Intent.getAction] of our [Intent] parameter [intent] is
     * [FirebaseAppIndex.ACTION_UPDATE_INDEX], we call the [AppIndexingUpdateService.enqueueWork]
     * method to schedule a job to update the Firebase App Index.
     *
     * @param context The [Context] in which the receiver is running.
     * @param intent The [Intent] being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (FirebaseAppIndex.ACTION_UPDATE_INDEX == intent?.action) {
            // Schedule job to run in the background.
            if (context != null) {
                AppIndexingUpdateService.enqueueWork(context)
            }
        }
    }
}