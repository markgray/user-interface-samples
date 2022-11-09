package com.example.android.appshortcuts

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Simple [CoroutineScope] extension function which fetches a [Bitmap] using the [CoroutineContext]
 * of [Dispatchers.IO] to execute the [doInBackground] lambda using its [Uri] parameter [uri] and
 * then calls the [onPostExecute] lambda with the [Bitmap] returned from [doInBackground] when the
 * background thread completes.
 *
 * @param doInBackground a lambda which takes a [Uri] and returns a [Bitmap].
 * @param onPostExecute a lambda which takes a [Bitmap] and returns nothing.
 * @param uri the [Uri] that will be passed to the [doInBackground] lambda.
 */
@Suppress("unused") // Suggested change would make class less reusable
fun CoroutineScope.getBitmapAndDisplay(
    doInBackground: (uri: Uri) -> Bitmap,
    onPostExecute: (Bitmap) -> Unit,
    uri: Uri
): Job = launch {
    val result = withContext(Dispatchers.IO) { // runs in background thread without blocking the Main Thread
        doInBackground(uri)
    }
    onPostExecute(result)
}

/**
 * Simple [CoroutineScope] extension function which takes no arguments, and returns nothing, but
 * executes its [doInBackground] lambda on the [Dispatchers.IO] thread pool, then calls its
 * [onPostExecute] lambda when [doInBackground] completes.
 *
 * @param doInBackground a lambda which takes no parameters and returns nothing.
 * @param onPostExecute a lambda which takes no parameters and returns nothing.
 */
fun CoroutineScope.noParamNoResultAsync(
    doInBackground: () -> Unit,
    onPostExecute: () -> Unit
): Job = launch {
    withContext(Dispatchers.IO) { // runs in background thread without blocking the Main Thread
        doInBackground()
    }
    onPostExecute()
}